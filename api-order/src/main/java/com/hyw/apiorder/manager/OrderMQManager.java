package com.hyw.apiorder.manager;

import cn.hutool.core.util.IdUtil;
import com.hyw.apicommon.constant.RabbitMqConstant;
import com.hyw.apicommon.constant.RedisConstant;
import com.hyw.apicommon.model.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author hyw
 * @create 2023-05-04 14:47
 */
@Component
@Slf4j
public class OrderMQManager implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private Long finalId = null;

    /**
     * 向mq发送订单消息
     *
     * @param order
     */
    public void sendOrderInfo(Order order) {
        finalId = order.getId();
        redisTemplate.opsForValue().set(RedisConstant.SEND_ORDER_PREFIX + order.getId(), order);
        String finalMessageId = IdUtil.simpleUUID();
        rabbitTemplate.convertAndSend(RabbitMqConstant.ORDER_EXCHANGE_NAME, RabbitMqConstant.ORDER_SEND_EXCHANGE_ROUTING_KEY, order, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            // 生成全局唯一id
            messageProperties.setMessageId(finalMessageId);
            messageProperties.setContentEncoding("utf-8");
            return message;
        });
    }

    /**
     * 只要消息抵达服务器，那么result=true
     *
     * @param correlationData 当前消息的唯一关联数据（消息的唯一id）
     * @param result               消息是否成功收到
     * @param reason               失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean result, String reason) {
        if (result) {
            // 投递成功则删除reids中的向mq发送订单的内容
            redisTemplate.delete(RedisConstant.SEND_ORDER_PREFIX + finalId);
        } else {
            log.error("订单--消息投递到服务端失败：{} ----> {}", correlationData, reason);
        }
    }

    @Override
    public void returnedMessage(ReturnedMessage returned) {
        log.error("发生异常，返回消息回调: {}", returned);
    }

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }
}
