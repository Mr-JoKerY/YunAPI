package com.hyw.project.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.hyw.apicommon.common.BaseResponse;
import com.hyw.apicommon.common.ResultUtils;
import com.hyw.apicommon.constant.RedisConstant;
import com.hyw.project.config.AliPayConfig;
import com.hyw.project.manager.OrderPaySuccessMQManager;
import com.hyw.project.model.dto.AlipayRequest;
import com.hyw.project.model.entity.AlipayInfo;
import com.hyw.project.service.AlipayInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author hyw
 */
@Controller
@RequestMapping("/alipay")
@Slf4j
public class AliPayController {

    @Resource
    private AliPayConfig aliPayConfig;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private AlipayInfoService alipayInfoService;

    @Resource
    private OrderPaySuccessMQManager orderPaySuccessMqManager;

    @GetMapping("/pay")
    public void pay(@RequestParam String outTradeNo, @RequestParam String subject, @RequestParam double totalAmount, HttpServletResponse response) throws AlipayApiException, IOException {
        AlipayClient alipayClient = new DefaultAlipayClient(aliPayConfig.getGatewayUrl(), aliPayConfig.getAppId(), aliPayConfig.getPrivateKey(), "json", aliPayConfig.getCharset(), aliPayConfig.getPublicKey(), aliPayConfig.getSignType());
        AlipayTradeWapPayRequest aliPayRequest = new AlipayTradeWapPayRequest();
        // 异步通知的地址
        aliPayRequest.setNotifyUrl(aliPayConfig.getNatUrl() + "/api/third/alipay/notify");

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", totalAmount);
        bizContent.put("subject", subject);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        aliPayRequest.setBizContent(bizContent.toString());
        AlipayTradeWapPayResponse aliPayResponse = alipayClient.pageExecute(aliPayRequest);
        if (aliPayResponse.isSuccess()) {
            System.out.println("调用成功");
            response.setContentType("text/html;charset=" + aliPayConfig.getCharset());
            String form = aliPayResponse.getBody();
            response.getWriter().write(form);
            response.getWriter().flush();
        } else {
            System.out.println("调用失败");
        }
    }

    @PostMapping("/payCode")
    @ResponseBody
    public BaseResponse<String> payCode(@RequestBody AlipayRequest alipayRequest) {
        String outTradeNo = alipayRequest.getOutTradeNo();
        String subject = alipayRequest.getSubject();
        double totalAmount = alipayRequest.getTotalAmount();

        log.info("内网穿透地址: {}", aliPayConfig.getNatUrl());
        String url = String.format(aliPayConfig.getNatUrl() + "/api/third/alipay/pay?outTradeNo=%s&subject=%s&totalAmount=%f", outTradeNo, subject, totalAmount);
        String base64 = QrCodeUtil.generateAsBase64(url, new QrConfig(300, 300), "png");
        return ResultUtils.success(base64);
    }

    /**
     * 查询交易结果
     *
     * @throws AlipayApiException
     */
    @PostMapping("/tradeQuery")
    public void tradeQuery() throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(aliPayConfig.getGatewayUrl(), aliPayConfig.getAppId(), aliPayConfig.getPrivateKey(), "json", aliPayConfig.getCharset(), aliPayConfig.getPublicKey(), aliPayConfig.getSignType());
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "111111111");
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            System.out.println("调用成功，结果：" + response.getBody());
        } else {
            System.out.println("调用失败");
        }
    }

    /**
     * 支付成功回调,注意这里必须是POST接口
     *
     * @param request
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/notify")
    public synchronized void payNotify(HttpServletRequest request) throws Exception {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            // 支付宝验签
            if (AlipaySignature.rsaCheckV1(params, aliPayConfig.getPublicKey(), aliPayConfig.getCharset(), aliPayConfig.getSignType())) {
                // 验证成功
                log.info("支付成功: {}", params);
                // 幂等性保证：判断该订单号是否被处理过
                Object outTradeNo = redisTemplate.opsForValue().get(RedisConstant.ALIPAY_TRADE_INFO + params.get("out_trade_no"));
                if (null == outTradeNo) {
                    // 验签通过，将订单信息存入数据库
                    AlipayInfo alipayInfo = new AlipayInfo();
                    alipayInfo.setSubject(params.get("subject"));
                    alipayInfo.setTradeStatus(params.get("trade_status"));
                    alipayInfo.setTradeNo(params.get("trade_no"));
                    alipayInfo.setOrderNumber(params.get("out_trade_no"));
                    alipayInfo.setTotalAmount(Double.valueOf(params.get("total_amount")));
                    alipayInfo.setBuyerId(params.get("buyer_id"));
                    alipayInfo.setGmtPayment(DateUtil.parse(params.get("gmt_payment")));
                    alipayInfo.setBuyerPayAmount(Double.valueOf(params.get("buyer_pay_amount")));
                    alipayInfoService.save(alipayInfo);
                    // 同时将交易结果存入redis中去，保证支付请求幂等性
                    redisTemplate.opsForValue().set(RedisConstant.ALIPAY_TRADE_INFO + alipayInfo.getOrderNumber(), alipayInfo, 30, TimeUnit.MINUTES);
                    // 修改数据库，完成整个订单功能
                    orderPaySuccessMqManager.sendOrderPaySuccess(params.get("out_trade_no"));
                }
            }
        }
    }

}
