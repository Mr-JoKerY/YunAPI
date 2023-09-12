package com.hyw.apiorder.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hyw.apicommon.model.entity.Order;
import com.hyw.apicommon.model.entity.User;
import com.hyw.apicommon.model.vo.OrderVO;
import com.hyw.apiorder.model.dto.OrderAddRequest;
import com.hyw.apiorder.model.dto.OrderQueryRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author hyw
* @description 针对表【order】的数据库操作Service
* @createDate 2023-05-03 15:52:09
*/
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     * @param addOrderRequest
     * @param request
     * @return
     */
    OrderVO addOrder(OrderAddRequest addOrderRequest, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param orderQueryRequest
     * @param request
     * @return
     */
    QueryWrapper<Order> getQueryWrapper(OrderQueryRequest orderQueryRequest, HttpServletRequest request);

    /**
     * 分页获取订单封装
     *
     * @param orderPage
     * @return
     */
    Page<OrderVO> getOrderVOPage(Page<Order> orderPage);

    /**
     * 获取前 limit 购买数量的接口
     * @param limit
     * @return
     */
    List<Order> listTopBuyInterfaceInfo(int limit);

}
