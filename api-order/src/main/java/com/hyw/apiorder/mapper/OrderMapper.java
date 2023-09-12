package com.hyw.apiorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hyw.apicommon.model.entity.Order;

import java.util.List;

/**
* @author hyw
* @description 针对表【order】的数据库操作Mapper
* @createDate 2023-05-03 15:52:09
*/
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 获取购买数量前 limit 的接口
     * @param limit
     * @return
     */
    List<Order> listTopBuyInterfaceInfo(int limit);

}




