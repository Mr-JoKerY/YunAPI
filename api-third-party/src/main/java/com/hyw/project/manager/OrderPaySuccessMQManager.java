package com.hyw.project.manager;

import com.hyw.apicommon.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

import static com.hyw.apicommon.constant.RabbitMqConstant.ORDER_EXCHANGE_NAME;
import static com.hyw.apicommon.constant.RabbitMqConstant.ORDER_SUCCESS_EXCHANGE_ROUTING_KEY;

/**
 * @author hyw
 */
@Component
@Slf4j
public class OrderPaySuccessMQManager {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private String finalId = null;

    /**
     * @param outTradeNo 我们自己的订单号
     */
    public void sendOrderPaySuccess(String outTradeNo) {
        finalId = outTradeNo;
        redisTemplate.opsForValue().set(RedisConstant.ORDER_PAY_SUCCESS_INFO + outTradeNo, outTradeNo);
        String finalMessageId = UUID.randomUUID().toString();
        rabbitTemplate.convertAndSend(ORDER_EXCHANGE_NAME, ORDER_SUCCESS_EXCHANGE_ROUTING_KEY, outTradeNo, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            // 生成全局唯一id
            messageProperties.setMessageId(finalMessageId);
            messageProperties.setContentEncoding("utf-8");
            return message;
        });
    }

}
