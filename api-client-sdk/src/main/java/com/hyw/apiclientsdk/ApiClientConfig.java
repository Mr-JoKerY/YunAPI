package com.hyw.apiclientsdk;

import com.hyw.apiclientsdk.client.CommonApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author hyw
 */
@Configuration
@ConfigurationProperties("api.client")
@Data
@ComponentScan
public class ApiClientConfig {

    private String accessKey;

    private String secretKey;

    @Bean
    public CommonApiClient apiClient() {
        return new CommonApiClient(accessKey, secretKey);
    }
}
