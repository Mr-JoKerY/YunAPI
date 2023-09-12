package com.hyw.apigateway.filter;

import cn.hutool.core.text.AntPathMatcher;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.google.common.util.concurrent.RateLimiter;
import com.hyw.apicommon.common.ErrorCode;
import com.hyw.apicommon.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.hyw.apicommon.constant.CookieConstant.*;

/**
 * 全局请求过滤器
 *
 * @author hyw
 */
@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    public static final List<String> NOT_LOGIN_PATH = Arrays.asList(
            "/api/user/login", "/api/user/login/sms", "/api/user/register", "/api/user/smsCaptcha",
            "/api/user/getCaptcha", "/api/interface/**", "/api/third/alipay/**", "/api/interfaceInfo/sdk"
    );

    @Resource
    private RateLimiter rateLimiter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getPath().value();

        // 限流过滤
        if (!rateLimiter.tryAcquire()) {
            log.error("请求太频繁了，被限流了!!!");
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }

        // 判断该接口是否需要登录
        List<Boolean> collect = NOT_LOGIN_PATH.stream().map(notLoginPath -> {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            return antPathMatcher.match(notLoginPath, path);
        }).collect(Collectors.toList());
        if (collect.contains(true)) {
            return chain.filter(exchange);
        }

        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        HttpCookie httpCookie = cookies.getFirst(TOKEN_KEY);
        if (httpCookie == null) {
            return handleNoAuth(response);
        }
        String cookie = httpCookie.toString();
        String[] split = cookie.split("=");
        cookie = split[1];
        if (!TOKEN_KEY.equals(split[0]) || StringUtils.isBlank(cookie)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // JWT验证
        if (!JWTUtil.verify(cookie, JWTSignerUtil.hs256(APP_SECRET.getBytes()))) {
            return handleNoAuth(response);
        }
        // 请求转发，响应日志
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }

}