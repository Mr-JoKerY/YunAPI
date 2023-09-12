package com.hyw.apiorder.listener;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hyw.apicommon.common.ErrorCode;
import com.hyw.apicommon.exception.BusinessException;
import com.hyw.apicommon.model.dto.UnLockAvailablePiecesRequest;
import com.hyw.apicommon.model.entity.Order;
import com.hyw.apicommon.service.InnerInterfaceChargingService;
import com.hyw.apiorder.model.entity.OrderLock;
import com.hyw.apiorder.model.enums.OrderLockStatusEnum;
import com.hyw.apiorder.model.enums.OrderStatusEnum;
import com.hyw.apiorder.service.OrderLockService;
import com.hyw.apiorder.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;

import static com.hyw.apicommon.constant.RabbitMqConstant.ORDER_TIMEOUT_QUEUE_NAME;

/**
 * 订单超时监听
 *
 * @author hyw
 */
@Slf4j
@Component
public class OrderTimeOutListener {

    @Resource
    private OrderService orderService;

    @Resource
    private OrderLockService orderLockService;

    @DubboReference
    private InnerInterfaceChargingService innerInterfaceChargingService;

    /**
     * 监听死信队列
     *
     * @param order
     * @param message
     * @param channel
     */
    @Transactional(rollbackFor = Exception.class)
    @RabbitListener(queues = ORDER_TIMEOUT_QUEUE_NAME)
    public void delayListener(Order order, Message message, Channel channel) throws IOException {
        try {
            log.error("监听到订单超时，死信队列消息 ==> {}", order);
            Long orderId = order.getId();
            Order dbOrder = orderService.getById(orderId);
            // 没有生成订单，但是库存已经扣了
            if (dbOrder == null) {
                unLockAvailablePieces(order);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            Integer status = dbOrder.getStatus();
            if (OrderStatusEnum.TOBEPAID.getValue() == status) {
                String orderNumber = order.getOrderNumber();
                // 超时未支付,更新订单表,订单锁表
                orderService.update(new UpdateWrapper<Order>().eq("id", orderId).set("status", OrderStatusEnum.FAILURE.getValue()));
                orderLockService.update(new UpdateWrapper<OrderLock>().eq("orderNumber", orderNumber).set("lockStatus", OrderLockStatusEnum.UNLOCK.getValue()));
                // 解锁库存
                unLockAvailablePieces(order);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("订单超时，死信队列报错：{}", e.getMessage());
            e.printStackTrace();
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    private void unLockAvailablePieces(Order order) {
        Long count = order.getCount();
        UnLockAvailablePiecesRequest unLockAvailablePiecesRequest = new UnLockAvailablePiecesRequest();
        unLockAvailablePiecesRequest.setCount(count);
        unLockAvailablePiecesRequest.setInterfaceId(order.getInterfaceId());
        boolean unLockAvailablePieces = innerInterfaceChargingService.unLockAvailablePieces(unLockAvailablePiecesRequest);
        if (!unLockAvailablePieces) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单超时，解锁接口库存失败！");
        }
    }
}
