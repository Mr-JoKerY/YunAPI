package com.hyw.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hyw.apicommon.model.entity.User;
import com.hyw.apicommon.model.vo.UserVO;
import com.hyw.project.model.dto.user.UserRegisterRequest;
import com.hyw.project.model.vo.LoginUserVO;
import com.hyw.project.model.vo.UserDevKeyVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户服务
 *
 * @author hyw
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @param request
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response);

    /**
     * 使用手机号登录
     *
     * @param phoneNum
     * @param phoneCaptcha
     * @param request
     * @return
     */
    LoginUserVO userLoginBySms(String phoneNum, String phoneCaptcha, HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @param response
     * @return
     */
    boolean userLogout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 生成图像验证码
     *
     * @param request
     * @param response
     */
    void getCaptcha(HttpServletRequest request, HttpServletResponse response);

    /**
     * 发送短信验证码
     *
     * @param phoneNum
     * @return
     */
    Boolean sendSmsCaptcha(String phoneNum);

    /**
     * 生成当前用户的ak,sk
     * @param request
     * @return
     */
    UserDevKeyVO genkey(HttpServletRequest request);

    /**
     * 通过 ak 获取用户
     * @param accessKey
     * @return
     */
    User getUserByAK(String accessKey);
}
