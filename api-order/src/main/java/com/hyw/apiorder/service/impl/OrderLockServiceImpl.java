package com.hyw.apiorder.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyw.apiorder.model.entity.OrderLock;
import com.hyw.apiorder.service.OrderLockService;
import com.hyw.apiorder.mapper.OrderLockMapper;
import org.springframework.stereotype.Service;

/**
* @author hyw
* @description 针对表【order_lock】的数据库操作Service实现
* @createDate 2023-05-03 15:52:09
*/
@Service
public class OrderLockServiceImpl extends ServiceImpl<OrderLockMapper, OrderLock>
    implements OrderLockService{

}




