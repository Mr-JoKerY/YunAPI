package com.hyw.apiorder.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hyw.apicommon.constant.RedisConstant;
import com.hyw.apicommon.model.dto.UpdateUserInterfaceInfoRequest;
import com.hyw.apicommon.model.entity.Order;
import com.hyw.apicommon.service.InnerUserInterfaceInfoService;
import com.hyw.apiorder.model.entity.OrderLock;
import com.hyw.apiorder.model.enums.OrderLockStatusEnum;
import com.hyw.apiorder.model.enums.OrderStatusEnum;
import com.hyw.apiorder.service.OrderLockService;
import com.hyw.apiorder.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.hyw.apicommon.constant.RabbitMqConstant.ORDER_SUCCESS_QUEUE_NAME;
import static com.hyw.apicommon.constant.RedisConstant.EXIST_KEY_VALUE;

/**
 * @author hyw
 */
@Slf4j
@Component
public class OrderPaySuccessListener {

    @Resource
    private OrderService orderService;

    @Resource
    private OrderLockService orderLockService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    // 生产者是懒加载机制，消费者是饿汉加载机制，二者机制不对应，所以消费者要自行创建队列并加载，否则会报错
    @Transactional(rollbackFor = Exception.class)
    @RabbitListener(queuesToDeclare = { @Queue(ORDER_SUCCESS_QUEUE_NAME)})
    public void alipaySuccess(String orderNum, Message message, Channel channel) throws IOException {
        try {
            // 1. 消息的可靠机制保障，如果消息成功被监听到说明消息已经成功由生产者将消息发送到队列中，不需要消息队列重新发送消息，删掉redis中对于消息的记录(发送端的消息可靠机制)
            redisTemplate.delete(RedisConstant.ORDER_PAY_SUCCESS_INFO + orderNum);
            // 2. 消费端的消息幂等性问题，因为消费端开启手动确认机制，会有重复消费的问题，这里使用redis记录已经成功处理的订单来解决(消费端的消息可靠机制)
            String result = redisTemplate.opsForValue().get(RedisConstant.ORDER_PAY_RABBITMQ + orderNum);
            if (StringUtils.isNotBlank(result)) {
                // 消费过了
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            log.info("支付成功：{}", orderNum);
            if (StringUtils.isNotBlank(orderNum)) {
                // 3. 修改订单状态
                orderService.update(new UpdateWrapper<Order>().eq("orderNumber", orderNum).set("status", OrderStatusEnum.DONE.getValue()));
                // 修改订单锁状态
                orderLockService.update(new UpdateWrapper<OrderLock>().eq("orderNumber", orderNum).set("lockStatus", OrderLockStatusEnum.DEDUCT.getValue()));

                Order order = orderService.getOne(new QueryWrapper<Order>().eq("orderNumber", orderNum));
                OrderLock orderLock = orderLockService.getOne(new QueryWrapper<OrderLock>().eq("orderNumber", orderNum));
                UpdateUserInterfaceInfoRequest updateUserInterfaceInfoRequest = new UpdateUserInterfaceInfoRequest();
                updateUserInterfaceInfoRequest.setUserId(order.getUserId());
                updateUserInterfaceInfoRequest.setInterfaceId(order.getInterfaceId());
                updateUserInterfaceInfoRequest.setLockNum(orderLock.getLockNum());
                // 4. 更新用户接口调用次数
                innerUserInterfaceInfoService.updateUserInterfaceInfo(updateUserInterfaceInfoRequest);
            }
            // 消费成功，设置redis中的状态
            // 5. 为解决消费端的消息幂等性问题，记录已经的成功处理的消息。30分钟后订单已经结束，淘汰记录的订单消息
            redisTemplate.opsForValue().set(RedisConstant.ORDER_PAY_RABBITMQ + orderNum, EXIST_KEY_VALUE, 30, TimeUnit.MINUTES);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("<====== 处理支付成功监听器出错 ======>");
            e.printStackTrace();
            redisTemplate.delete(RedisConstant.ORDER_PAY_RABBITMQ + orderNum);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
