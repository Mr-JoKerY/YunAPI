package com.hyw.project.manager;

import com.hyw.apicommon.constant.RedisConstant;
import com.hyw.apicommon.model.dto.SendSmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务
 *
 * @author hyw
 */
@Component
@Slf4j
public class SmsManager {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private RedisTokenBucketManager redisTokenBucket;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private SmsMQManager smsMqManager;

    public boolean sendSms(SendSmsRequest smsRequest) {
        // 从令牌桶中取得令牌，未取得不允许发送短信
        // 法一：Guava RateLimiter实现单机限流
//        RateLimiter rateLimiter = RateLimiter.create(1.0 / 60);
//        boolean acquire = rateLimiter.tryAcquire(1);
        // 法二：基于redisson的分布式限流（多机限流）
        // 限流判断，每个用户一个限流器
//        redisLimiterManager.doRateLimit("sendSms_" + loginUser.getId());
        // 法三：基于redis自实现令牌桶算法
        boolean acquire = redisTokenBucket.tryAcquire(smsRequest.getPhoneNum());
        if (!acquire) {
            log.info("phoneNum：{}，send SMS frequent", smsRequest.getPhoneNum());
            return false;
        }
        log.info("发送短信：{}", smsRequest);
        String phoneNum = smsRequest.getPhoneNum();
        String code = smsRequest.getCode();

        // 将手机号对应的验证码存入Redis，方便后续检验
        redisTemplate.opsForValue().set(RedisConstant.SMS_CODE_PREFIX + phoneNum, String.valueOf(code), 5, TimeUnit.MINUTES);

        // 利用消息队列，异步发送短信
        smsMqManager.sendSmsAsync(smsRequest);
        return true;
    }

    public boolean verifyCode(String phoneNum, String code) {
        String key = RedisConstant.SMS_CODE_PREFIX + phoneNum;
        String checkCode = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(code) && code.equals(checkCode)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
