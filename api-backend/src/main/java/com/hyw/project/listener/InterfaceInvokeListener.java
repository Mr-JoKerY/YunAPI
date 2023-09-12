package com.hyw.project.listener;

import com.hyw.apicommon.model.dto.UpdateUserInterfaceInfoRequest;
import com.hyw.project.service.UserInterfaceInfoService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

import static com.hyw.apicommon.constant.RabbitMqConstant.INTERFACE_QUEUE_NAME;


/**
 * 接口调用监听器，如果接口调用失败则需要回滚数据库的接口统计数据
 */
@Component
@Slf4j
public class InterfaceInvokeListener {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;


    // 监听队列，实现接口统计功能
    // 生产者是懒加载机制，消费者是饿汉加载机制，二者机制不对应，所以消费者要自行创建队列并加载，否则会报错
    @RabbitListener(queuesToDeclare = {@Queue(INTERFACE_QUEUE_NAME)})
    public void receiveSms(UpdateUserInterfaceInfoRequest request, Message message, Channel channel) throws IOException {
        log.info("监听到消息啦，内容是：{}", request);
        Long interfaceInfoId = request.getInterfaceId();
        Long userId = request.getUserId();

        boolean result;
        try {
            result = userInterfaceInfoService.recoverInvokeCount(interfaceInfoId, userId);
        } catch (Exception e) {
            log.error("接口统计数据回滚失败!!!", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            return;
        }
        if (!result) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}