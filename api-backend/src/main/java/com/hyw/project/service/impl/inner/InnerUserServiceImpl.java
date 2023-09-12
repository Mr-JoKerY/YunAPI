package com.hyw.project.service.impl.inner;

import com.hyw.apicommon.model.entity.User;
import com.hyw.apicommon.service.InnerUserService;
import com.hyw.project.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    /**
     * 通过 ak 获取调用者
     * @param accessKey
     * @return
     */
    @Override
    public User getInvokeUser(String accessKey) {
        return userService.getUserByAK(accessKey);
    }
}
