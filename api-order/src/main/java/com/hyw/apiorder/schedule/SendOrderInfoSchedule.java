package com.hyw.apiorder.schedule;

import com.hyw.apicommon.common.ErrorCode;
import com.hyw.apicommon.constant.RedisConstant;
import com.hyw.apicommon.exception.BusinessException;
import com.hyw.apicommon.model.entity.Order;
import com.hyw.apiorder.manager.OrderMQManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.hyw.apicommon.constant.LockConstant.ORDER_SEND_FAILED;


/**
 * 订单发送失败重试定时任务
 *
 * @author hyw
 */
@EnableAsync
@EnableScheduling
@Component
@Slf4j
public class SendOrderInfoSchedule {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private OrderMQManager orderMqManager;

    @Scheduled(cron = "*/60 * * * * ?")
    public void sendOrderInfoFailed() {
        RLock lock = redissonClient.getLock(ORDER_SEND_FAILED);
        try {
            // 为加锁等待20秒时间，并在加锁成功10秒钟后自动解开
            boolean tryLock = lock.tryLock(20, 10, TimeUnit.SECONDS);
            if (tryLock) {
                // 重新向mq中发送订单的消息
                Set keys = redisTemplate.keys(RedisConstant.SEND_ORDER_PREFIX + "*");
                for (Object key : keys) {
                    Order order = (Order) redisTemplate.opsForValue().get(key);
                    // 删除reids中的该条记录
                    redisTemplate.delete(key.toString());
                    orderMqManager.sendOrderInfo(order);
                }
            }
        } catch (InterruptedException e) {
            log.error("=== 定时任务:获取失败生产者发送消息redis出现bug ===");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}
