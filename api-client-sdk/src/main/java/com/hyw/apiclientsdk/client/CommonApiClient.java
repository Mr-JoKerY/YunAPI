package com.hyw.apiclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.google.gson.Gson;
import com.hyw.apiclientsdk.common.ApiRequest;
import com.hyw.apiclientsdk.common.ApiResponse;
import com.hyw.apiclientsdk.constant.MethodConstant;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.hyw.apiclientsdk.utils.SignUtils.genSign;

/**
 * API的公共客户端
 *
 * @author hyw
 */
public class CommonApiClient {

    protected final String accessKey;

    protected final String secretKey;

    protected static final String GATEWAY_HOST = "http://localhost:8090";

    public CommonApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    protected Map<String, String> getHeaderMap(String body) {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("accessKey", accessKey);
        // 一定不能直接发送
//        hashMap.put("secretKey", secretKey);
        hashMap.put("nonce", RandomUtil.randomNumbers(4));
        hashMap.put("body", body);
        hashMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        hashMap.put("sign", genSign(body, secretKey));
        return hashMap;
    }

    public ApiResponse sendRequest(ApiRequest request) {
        String method = request.getMethod().toLowerCase();
        String path = request.getPath();
        String params = request.getParams();

        String url = GATEWAY_HOST + URLUtil.getPath(path);
        HttpResponse httpResponse = null;
        if (MethodConstant.REQUEST_METHOD_GET.equals(method)) {
            Gson gson = new Gson();
            Map paramsMap = gson.fromJson(params, Map.class);

            httpResponse = HttpRequest.get(url)
                    .addHeaders(getHeaderMap(params))
                    .formStr(paramsMap)
                    .execute();
        } else if (MethodConstant.REQUEST_METHOD_POST.equals(method)) {
            httpResponse = HttpRequest.post(url)
                    .addHeaders(getHeaderMap(params))
                    .body(params)
                    .execute();
        }

        ApiResponse response = new ApiResponse();
        response.setResult("");
        if (httpResponse != null) {
            response.setResult(httpResponse.body());
        }
        return response;
    }

    // 将map型转为请求参数型
    protected static String urlencode(Map<String, String> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
