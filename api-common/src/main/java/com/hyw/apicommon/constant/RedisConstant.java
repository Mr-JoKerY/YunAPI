package com.hyw.apicommon.constant;

/**
 * Redis常量
 *
 * @author hyw
 */
public interface RedisConstant {

    // 表示redis中key对应的value存在
    String EXIST_KEY_VALUE = "1";

    // 图片验证码
    String CAPTCHA_PREFIX = "api:captchaId:";

    String SMS_PREFIX = "sms:";
    String SMS_BUCKET_PREFIX = SMS_PREFIX + "bucket:";
    String SMS_CODE_PREFIX = SMS_PREFIX + "code:";
    String SMS_MESSAGE_PREFIX = SMS_PREFIX + "mq:messageId:";
    String MQ_PRODUCER = SMS_PREFIX + "mq:producer:fail";

    String SEND_ORDER_PREFIX = "order:sendOrderNumInfo:";
    String ORDER_PAY_SUCCESS_INFO = "order:paySuccess:";
    String ORDER_PAY_RABBITMQ = "order:pay:rabbitMq:";
    String ALIPAY_TRADE_INFO = "alipay:tradeInfo:";
}
