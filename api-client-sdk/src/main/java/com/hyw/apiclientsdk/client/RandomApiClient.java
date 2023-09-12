package com.hyw.apiclientsdk.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

/**
 * RandomController-RandomApiClient
 *
 * @author hyw
 */
public class RandomApiClient extends CommonApiClient {

    public RandomApiClient(String accessKey, String secretKey) {
        super(accessKey, secretKey);
    }

    /**
     * 获取随机文本
     *
     * @return
     */
    public String getRandomWork() {
        HttpResponse httpResponse = HttpRequest.get(GATEWAY_HOST + "/api/interface/random/word")
                .addHeaders(getHeaderMap(""))
                .execute();
        System.out.println(httpResponse.getStatus());
        String result = httpResponse.body();
        System.out.println(result);
        return result;
    }

    /**
     * 获取随机动漫图片
     *
     * @return
     */
    public String getRandomImageUrl() {
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + "/api/interface/random/image")
                .addHeaders(getHeaderMap(""))
                .execute();
        System.out.println(httpResponse.getStatus());
        String result = httpResponse.body();
        System.out.println(result);
        return result;
    }
}
