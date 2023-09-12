package com.hyw.apigateway.config;

import com.hyw.apigateway.filter.InterfaceInvokeFilter;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Resource;

/**
 * @author hyw
 */
@Configuration
public class GatewayConfig {

    @Resource
    private InterfaceInvokeFilter interfaceInvokeFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("api_interface", r -> r.path("/api/interface/**")
                        .filters(f -> f.filter(interfaceInvokeFilter))
                        .uri("lb://api-interface"))
                .build();
    }

}
