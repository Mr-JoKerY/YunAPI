package com.hyw.apiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyw.apiinterface.entity.CommonResponse;
import com.hyw.apiinterface.entity.Image;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/day")
public class DayController {

    @PostMapping("/wallpaper")
    public String getDayWallpaperUrl() {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("format", "json");
        HttpResponse response = HttpRequest.post("https://tenapi.cn/v2/bing")
                .form(paramMap)
                .execute();
        String body = response.body();
        Gson gson = new Gson();
        CommonResponse<Image> imageResponse = gson.fromJson(body, new TypeToken<CommonResponse<Image>>(){}.getType());
        return imageResponse.getData().getUrl();
    }
}
