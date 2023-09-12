package com.hyw.project.manager;

import com.hyw.apicommon.constant.RedisConstant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * redis实现令牌桶
 *
 * @author hyw
 */
@Component
public class RedisTokenBucketManager {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    // 令牌桶容量
    public final long tokenCapacity = 1L;

    // 生产令牌的速率
    private final int putTokenRate = 1;

    /**
     * 过期时间，400秒后过期
     */
    private final long EXPIRE_TIME = 400;

    private final String TOKEN_SUFFIX = "_tokens";
    private final String REFRESH_TIME_SUFFIX = "_refresh_time";

    /**
     * 令牌桶算法，一分钟以内，每个手机号只能发送一次
     *
     * @param phoneNum
     * @return
     */
    public boolean tryAcquire(String phoneNum) {
        String key = RedisConstant.SMS_BUCKET_PREFIX + phoneNum;
        String tokenKey = key + TOKEN_SUFFIX;
        String refreshTimeKey = key + REFRESH_TIME_SUFFIX;
        /**
         * 获取[上次向桶中投放令牌的时间]，如果没有设置过这个投放时间，则令牌桶也不存在，此时：
         * 一种情况是：首次执行，此时定义令牌桶就是满的。
         * 另一种情况是：较长时间没有执行过限流处理，导致承载这个时间的KV被释放了，
         * 这个过期时间会超过自然投放令牌到桶中直到桶满的时间，所以令牌桶也应该是满的。
         */
        if (Boolean.FALSE.equals(redisTemplate.hasKey(refreshTimeKey))) {
            redisTemplate.opsForValue().set(tokenKey, String.valueOf(tokenCapacity - 1), EXPIRE_TIME, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(refreshTimeKey, String.valueOf(System.currentTimeMillis()), EXPIRE_TIME, TimeUnit.SECONDS);
            return true;
        }
        // 计算令牌桶内令牌数
        long currentToken = Long.parseLong(redisTemplate.opsForValue().get(tokenKey) == null ? "0" : redisTemplate.opsForValue().get(tokenKey));
        // 计算令牌桶上次刷新时间
        long refreshTime = Long.parseLong(redisTemplate.opsForValue().get(refreshTimeKey));

        // 获取系统当前时间
        long currentTime = System.currentTimeMillis();
        // 生成的令牌 = (当前时间 - 上次刷新时间) * 放入令牌的速率
        long generateToken = (currentTime - refreshTime) / 1000 * putTokenRate / 60;
        // 更新令牌桶内令牌数
        currentToken = Math.min(generateToken + currentToken, tokenCapacity);
        // 刷新时间
        refreshTime = currentTime;
        // 桶里面还有令牌，请求正常处理
        if (currentToken > 0) {
            currentToken--;
            redisTemplate.opsForValue().set(tokenKey, String.valueOf(currentToken), EXPIRE_TIME, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(refreshTimeKey, String.valueOf(refreshTime), EXPIRE_TIME, TimeUnit.SECONDS);
            // 如果获取到令牌，则返回true
            return true;
        }
        // 如果没有获取到令牌，则返回false
        return false;
    }
}
