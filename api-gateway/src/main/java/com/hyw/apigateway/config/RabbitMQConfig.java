package com.hyw.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.hyw.apicommon.constant.RabbitMqConstant.*;


/**
 * 接口数据一致性补偿消息相关队列定义
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    // 声明队列
    @Bean("INTERFACE_QUEUE_NAME")
    public Queue interfaceQueue() {
        return new Queue(INTERFACE_QUEUE_NAME, true, false, false);
    }

    // 声明交换机
    @Bean("INTERFACE_EXCHANGE_NAME")
    public Exchange interfaceExchange() {
        return new DirectExchange(INTERFACE_EXCHANGE_NAME, true, false);
    }

    // 交换机绑定队列
    @Bean
    public Binding interfaceBinding() {
        return new Binding(INTERFACE_QUEUE_NAME, Binding.DestinationType.QUEUE, INTERFACE_EXCHANGE_NAME, INTERFACE_EXCHANGE_ROUTING_KEY, null);
    }
}