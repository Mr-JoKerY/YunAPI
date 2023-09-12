package com.hyw.project.config;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云短信服务客户端
 *
 * @author hyw
 */
@Configuration
@ConfigurationProperties(prefix = "sms.client")
@Data
public class SmsClientConfig {

    private String secretId;

    private String secretKey;

    private String region;

    private String sdkAppId;

    private String signName;

    private String templateId;

    @Bean
    public SmsClient smsClient() {
        // 初始化用户身份信息(secretId, secretKey)
        Credential cred = new Credential(secretId, secretKey);
        return new SmsClient(cred, region);
    }
}
