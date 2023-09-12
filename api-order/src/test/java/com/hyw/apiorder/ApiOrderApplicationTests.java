package com.hyw.apiorder;

import com.hyw.apicommon.model.entity.Order;
import com.hyw.apiorder.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class ApiOrderApplicationTests {

    @Resource
    OrderMapper orderMapper;
    @Test
    void contextLoads() {
        List<Order> orders = orderMapper.listTopBuyInterfaceInfo(3);
        System.out.println(orders);
    }

}
