package com.hyw.apicommon.service;

import com.hyw.apicommon.model.entity.Order;

import java.util.List;

/**
 * @author hyw
 * @create 2023-05-11 22:57
 */
public interface InnerOrderService {
    
    /**
     * 获取前 limit 购买数量的接口
     *
     * @param limit
     * @return
     */
    List<Order> listTopBuyInterfaceInfo(int limit);
}
