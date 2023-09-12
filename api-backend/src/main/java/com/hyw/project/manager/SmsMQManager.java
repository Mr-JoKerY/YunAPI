package com.hyw.project.manager;

import com.hyw.apicommon.constant.RabbitMqConstant;
import com.hyw.apicommon.constant.RedisConstant;
import com.hyw.apicommon.model.dto.SendSmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 向mq发送消息，并进行保证消息可靠性处理
 *
 * @author hyw
 */
@Component
@Slf4j
public class SmsMQManager implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private String finalId = null;

    private SendSmsRequest smsRequest = null;

    /**
     * 向mq中投递发送短信消息
     *
     * @param smsRequest
     * @throws Exception
     */
    public void sendSmsAsync(SendSmsRequest smsRequest) {
        String messageId = null;
        try {
            // 将 headers 添加到 MessageProperties 中，并发送消息
            messageId = UUID.randomUUID().toString();
            HashMap<String, Object> messageArgs = new HashMap<>();
            messageArgs.put("retryCount", 0);
            // 消息状态：0-未投递、1-已投递
            messageArgs.put("status", 0);
            messageArgs.put("smsTo", smsRequest);
            // 将重试次数和短信发送状态存入redis中去,并设置过期时间
            redisTemplate.opsForHash().putAll(RedisConstant.SMS_MESSAGE_PREFIX + messageId, messageArgs);
            redisTemplate.expire(RedisConstant.SMS_MESSAGE_PREFIX + messageId, 10, TimeUnit.MINUTES);

            String finalMessageId = messageId;
            finalId = messageId;
            this.smsRequest = smsRequest;
            // 将消息投递到MQ，并设置消息的一些参数
            rabbitTemplate.convertAndSend(RabbitMqConstant.SMS_EXCHANGE_NAME, RabbitMqConstant.SMS_EXCHANGE_ROUTING_KEY, smsRequest, message -> {
                MessageProperties messageProperties = message.getMessageProperties();
                // 生成全局唯一id
                messageProperties.setMessageId(finalMessageId);
                messageProperties.setContentEncoding("utf-8");
                return message;
            });
        } catch (Exception e) {
            // 出现异常，删除该短信id对应的redis
            redisTemplate.delete(RedisConstant.SMS_MESSAGE_PREFIX + messageId);
            // 将该失败消息存入到“死信”redis中去，然后使用定时任务去扫描该key，并重新发送到mq中去(待做)
            redisTemplate.opsForHash().put(RedisConstant.MQ_PRODUCER, messageId, smsRequest);
            throw new RuntimeException(e);
        }
    }

    /**
     * 发布者确认的回调
     *
     * @param correlationData 回调的相关数据。
     * @param result               ack为真，nack为假
     * @param s               一个可选的原因，用于nack，如果可用，否则为空。
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean result, String s) {
        // 消息发送成功，将redis中消息的状态（status）修改为1
        if (result) {
            redisTemplate.opsForHash().put(RedisConstant.SMS_MESSAGE_PREFIX + finalId, "status", 1);
        } else {
            // 发送失败，放入redis失败集合中，并删除集合数据
            log.error("短信消息投送失败：{}-->{}", correlationData, s);
            redisTemplate.delete(RedisConstant.SMS_MESSAGE_PREFIX + finalId);
            redisTemplate.opsForHash().put(RedisConstant.MQ_PRODUCER, finalId, this.smsRequest);
        }
    }

    /**
     * 发生异常时的消息返回提醒
     *
     * @param returnedMessage
     */
    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.error("发生异常，返回消息回调:{}", returnedMessage);
        // 发送失败，放入redis失败集合中，并删除集合数据
        redisTemplate.delete(RedisConstant.SMS_MESSAGE_PREFIX + finalId);
        redisTemplate.opsForHash().put(RedisConstant.MQ_PRODUCER, finalId, this.smsRequest);
    }

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }
}
