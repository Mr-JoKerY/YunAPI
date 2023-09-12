package com.hyw.apiorder.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.hyw.apicommon.common.ErrorCode;
import com.hyw.apicommon.constant.CommonConstant;
import com.hyw.apicommon.exception.BusinessException;
import com.hyw.apicommon.exception.ThrowUtils;
import com.hyw.apicommon.model.dto.LockAvailablePiecesRequest;
import com.hyw.apicommon.model.entity.InterfaceInfo;
import com.hyw.apicommon.model.entity.Order;
import com.hyw.apicommon.model.entity.User;
import com.hyw.apicommon.model.vo.OrderVO;
import com.hyw.apicommon.service.InnerInterfaceChargingService;
import com.hyw.apicommon.service.InnerInterfaceInfoService;
import com.hyw.apicommon.utils.JwtUtils;
import com.hyw.apicommon.utils.SqlUtils;
import com.hyw.apiorder.manager.OrderMQManager;
import com.hyw.apiorder.mapper.OrderMapper;
import com.hyw.apiorder.model.dto.OrderAddRequest;
import com.hyw.apiorder.model.dto.OrderQueryRequest;
import com.hyw.apiorder.model.entity.OrderLock;
import com.hyw.apiorder.model.enums.OrderLockStatusEnum;
import com.hyw.apiorder.model.enums.OrderStatusEnum;
import com.hyw.apiorder.service.OrderLockService;
import com.hyw.apiorder.service.OrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.hyw.apicommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author hyw
 * @description 针对表【order】的数据库操作Service实现
 * @createDate 2023-05-03 15:52:09
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @DubboReference
    private InnerInterfaceChargingService innerInterfaceChargingService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderLockService orderLockService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private OrderMQManager orderMqManager;

    private final static Gson GSON = new Gson();

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 200, 10, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(100000), Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());

    /**
     * 创建订单
     * @param orderAddRequest
     * @param request
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO addOrder(OrderAddRequest orderAddRequest, HttpServletRequest request) {
        OrderVO orderVO = null;
        try {
            Long interfaceId = orderAddRequest.getInterfaceId();
            Long chargingId = orderAddRequest.getChargingId();
            Long userId = orderAddRequest.getUserId();
            Long count = orderAddRequest.getCount();
            Double charging = orderAddRequest.getCharging();
            Double totalAmount = orderAddRequest.getTotalAmount();
            if (null == interfaceId || null == userId || null == count || null == charging || null == totalAmount || null == chargingId) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }

            // 校验登录用户与购买用户是否相符
            User loginUser = getLoginUser(request);
            if (loginUser == null || !userId.equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }

            // 异步获取接口信息
            InterfaceInfo interfaceInfo = new InterfaceInfo();
            CompletableFuture<Void> getInterfaceInfoFuture = CompletableFuture.runAsync(() -> {
                InterfaceInfo interfaceInfoById = innerInterfaceInfoService.getInterfaceInfoById(interfaceId);
                interfaceInfo.setName(interfaceInfoById.getName());
                interfaceInfo.setDescription(interfaceInfoById.getDescription());
            }, executor);

            // 生成订单号
            String orderNum = generateOrderNum(userId);
            // 总价 保留两位小数 四舍五入
            BigDecimal costSum = new BigDecimal(charging * count);
            double finalPrice = costSum.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (finalPrice != totalAmount) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "价格错误!");
            }

            // 锁定接口库存
            LockAvailablePiecesRequest lockAvailablePiecesRequest = new LockAvailablePiecesRequest();
            lockAvailablePiecesRequest.setChargingId(chargingId);
            lockAvailablePiecesRequest.setCount(count);
            boolean isLockOrder;
            try {
                isLockOrder = innerInterfaceChargingService.lockAvailablePieces(lockAvailablePiecesRequest);
            } catch (BusinessException e) {
                throw e;
            }
            if (!isLockOrder) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "锁定订单失败!");
            }

            // 持久化订单锁
            OrderLock orderLock = new OrderLock();
            orderLock.setOrderNumber(orderNum);
            orderLock.setChargingId(chargingId);
            orderLock.setUserId(userId);
            orderLock.setLockNum(count);
            orderLock.setLockStatus(OrderLockStatusEnum.LOCK.getValue());
            orderLockService.save(orderLock);

            // 创建订单
            Order order = new Order();
            order.setInterfaceId(interfaceId);
            order.setUserId(userId);
            order.setOrderNumber(orderNum);
            order.setCount(count);
            order.setCharging(charging);
            order.setTotalAmount(finalPrice);
            order.setStatus(OrderStatusEnum.TOBEPAID.getValue());
            this.save(order);

            // 将订单发送到延迟队列（注意：该订单消息无需监听消费，等消息过期，通过死信队列的监听器，来修改订单状态）
            orderMqManager.sendOrderInfo(order);

            // 等待异步任务返回
            CompletableFuture.allOf(getInterfaceInfoFuture).get();

            orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setInterfaceName(interfaceInfo.getName());
            orderVO.setInterfaceDesc(interfaceInfo.getDescription());
            DateTime date = DateUtil.date();
            orderVO.setCreateTime(date);
            orderVO.setExpirationTime(DateUtil.offset(date, DateField.MINUTE, 30));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建订单失败: " + e.getMessage());
        }
        return orderVO;
    }

    /**
     * 获取查询包装类
     *
     * @param orderQueryRequest
     * @param request
     * @return
     */
    @Override
    public QueryWrapper<Order> getQueryWrapper(OrderQueryRequest orderQueryRequest, HttpServletRequest request) {
        if (orderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getId();
        Integer status = orderQueryRequest.getType();
        String sortField = orderQueryRequest.getSortField();
        String sortOrder = orderQueryRequest.getSortOrder();
        // 排除不合法的状态
        ThrowUtils.throwIf(!OrderStatusEnum.getValues().contains(status), ErrorCode.PARAMS_ERROR);

        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    /**
     * 分页获取订单封装
     *
     * @param orderPage
     * @return
     */
    @Override
    public Page<OrderVO> getOrderVOPage(Page<Order> orderPage) {
        List<Order> orderList = orderPage.getRecords();
        Page<OrderVO> orderVOPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        if (CollectionUtils.isEmpty(orderList)) {
            return orderVOPage;
        }
        // 填充信息
        List<OrderVO> orderVOList = orderList.stream().map(order -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);

            Long interfaceId = order.getInterfaceId();
            InterfaceInfo interfaceInfo = innerInterfaceInfoService.getInterfaceInfoById(interfaceId);
            orderVO.setInterfaceName(interfaceInfo.getName());
            orderVO.setInterfaceDesc(interfaceInfo.getDescription());
            orderVO.setExpirationTime(DateUtil.offset(order.getCreateTime(), DateField.MINUTE, 30));
            return orderVO;
        }).collect(Collectors.toList());
        orderVOPage.setRecords(orderVOList);
        return orderVOPage;
    }

    @Override
    public List<Order> listTopBuyInterfaceInfo(int limit) {
        return orderMapper.listTopBuyInterfaceInfo(limit);
    }

    /**
     * 生成订单号(雪花算法)
     *
     * @return
     */
    private String generateOrderNum(Long userId) {
        String timeId = IdWorker.getTimeId();
        String substring = timeId.substring(0, timeId.length() - 15);
        return substring + RandomUtil.randomNumbers(5) + userId;
    }

    /**
     * 获取登录用户
     * @param request
     * @return
     */
    private User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Long userId = JwtUtils.getUserIdByToken(request);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        String userJson = redisTemplate.opsForValue().get(USER_LOGIN_STATE + userId);
        User currentUser = GSON.fromJson(userJson, User.class);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

}




