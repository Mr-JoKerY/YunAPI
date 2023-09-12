package com.hyw.apiclientsdk.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

/**
 * DayController-DayApiClient
 *
 * @author hyw
 */
public class DayApiClient extends CommonApiClient {

    public DayApiClient(String accessKey, String secretKey) {
        super(accessKey, secretKey);
    }

    /**
     * 获取每日壁纸
     *
     * @return
     */
    public String getDayWallpaperUrl() {
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + "/api/interface/day/wallpaper")
                .addHeaders(getHeaderMap(""))
                .execute();
        System.out.println(httpResponse.getStatus());
        String result = httpResponse.body();
        System.out.println(result);
        return result;
    }
}
