package com.hyw.apiorder.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hyw.apicommon.common.BaseResponse;
import com.hyw.apicommon.common.ErrorCode;
import com.hyw.apicommon.common.ResultUtils;
import com.hyw.apicommon.exception.BusinessException;
import com.hyw.apicommon.exception.ThrowUtils;
import com.hyw.apicommon.model.entity.Order;
import com.hyw.apicommon.model.entity.User;
import com.hyw.apicommon.model.vo.OrderVO;
import com.hyw.apicommon.model.vo.UserVO;
import com.hyw.apicommon.service.InnerUserService;
import com.hyw.apiorder.model.dto.OrderAddRequest;
import com.hyw.apiorder.model.dto.OrderQueryRequest;
import com.hyw.apiorder.service.OrderService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 订单接口
 *
 * @author hyw
 */
@RestController
@RequestMapping("/")
public class OrderController {

    @Resource
    private OrderService orderService;

    @PostMapping("/add")
    public BaseResponse<OrderVO> addOrder(@RequestBody OrderAddRequest orderAddRequest, HttpServletRequest request) {
        if (orderAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        OrderVO orderVO = orderService.addOrder(orderAddRequest, request);
        return ResultUtils.success(orderVO);
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<OrderVO>> listOrderByPage(@RequestBody OrderQueryRequest orderQueryRequest, HttpServletRequest request) {
        if (orderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = orderQueryRequest.getCurrent();
        long size = orderQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Order> orderPage = orderService.page(new Page<>(current, size), orderService.getQueryWrapper(orderQueryRequest, request));
        return ResultUtils.success(orderService.getOrderVOPage(orderPage));
    }
}
