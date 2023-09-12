package com.hyw.apiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyw.apiinterface.entity.Comment;
import com.hyw.apiinterface.entity.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/music")
public class MusicController {

    @GetMapping("/comment")
    public String getMusicComment() {
        HttpResponse response = HttpRequest.get("https://tenapi.cn/v2/comment")
                .execute();
        String body = response.body();
        Gson gson = new Gson();
        CommonResponse<Comment> result = gson.fromJson(body, new TypeToken<CommonResponse<Comment>>(){}.getType());
        return result.getData().getSongs();
    }

}
