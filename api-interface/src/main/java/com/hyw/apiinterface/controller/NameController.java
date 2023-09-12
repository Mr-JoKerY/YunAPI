package com.hyw.apiinterface.controller;

import com.hyw.apiclientsdk.model.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 名称api
 *
 * @author hyw
 */
@RestController
@RequestMapping("/name")
public class NameController {

    @PostMapping("/user")
    public String getName(@RequestBody User user) {
        String result = "你的名字是" + user.getUsername();
        return result;
    }
}
