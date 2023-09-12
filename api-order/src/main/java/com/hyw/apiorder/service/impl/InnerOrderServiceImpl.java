package com.hyw.apiorder.service.impl;

import com.hyw.apicommon.model.entity.Order;
import com.hyw.apicommon.service.InnerOrderService;
import com.hyw.apiorder.service.OrderService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hyw
 * @create 2023-05-11 23:00
 */
@DubboService
public class InnerOrderServiceImpl implements InnerOrderService {

    @Resource
    private OrderService orderService;

    @Override
    public List<Order> listTopBuyInterfaceInfo(int limit) {
        return orderService.listTopBuyInterfaceInfo(limit);
    }
}
