package com.hyw.project.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.hyw.apicommon.common.ErrorCode;
import com.hyw.apicommon.constant.CookieConstant;
import com.hyw.apicommon.constant.RedisConstant;
import com.hyw.apicommon.constant.UserConstant;
import com.hyw.apicommon.exception.BusinessException;
import com.hyw.apicommon.exception.ThrowUtils;
import com.hyw.apicommon.model.dto.SendSmsRequest;
import com.hyw.apicommon.model.entity.User;
import com.hyw.apicommon.model.vo.UserVO;
import com.hyw.apicommon.utils.JwtUtils;
import com.hyw.project.manager.SmsManager;
import com.hyw.project.mapper.UserMapper;
import com.hyw.project.model.dto.user.UserRegisterRequest;
import com.hyw.project.model.vo.LoginUserVO;
import com.hyw.project.model.vo.UserDevKeyVO;
import com.hyw.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hyw.apicommon.constant.CookieConstant.TOKEN_KEY;
import static com.hyw.apicommon.constant.UserConstant.USERNAME_LENGTH;
import static com.hyw.apicommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author hyw
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private SmsManager smsManager;

    private final static Gson GSON = new Gson();

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @param request
     * @return 新用户 id
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userName = userRegisterRequest.getUserName();
        String phoneNum = userRegisterRequest.getPhoneNum();
        String phoneCaptcha = userRegisterRequest.getPhoneCaptcha();
        String captcha = userRegisterRequest.getCaptcha();

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userName, phoneNum, phoneCaptcha, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (userName.length() > USERNAME_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户昵称过长");
        }
        // 手机验证码
        if (!Validator.isMobile(phoneNum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机验证码错误");
        }
        if (!smsManager.verifyCode(phoneNum, phoneCaptcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机验证码过期或错误！");
        }
        // 图形验证码
        String signature = request.getHeader("signature");
        if (StringUtils.isEmpty(signature)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String checkCaptcha = redisTemplate.opsForValue().get(RedisConstant.CAPTCHA_PREFIX + signature);
        if (StringUtils.isEmpty(checkCaptcha) || !captcha.equals(checkCaptcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片验证码过期或错误！");
        }

        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());
            // 3. 分配 accessKey，secretKey
            UserDevKeyVO userDevKeyVO = genKey(userAccount);
            String accessKey = userDevKeyVO.getAccessKey();
            String secretKey = userDevKeyVO.getSecretKey();
            // 4. 插入数据
            User user = new User();
            user.setUserName(userName);
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            user.setPhoneNum(phoneNum);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        return setLoginUser(user, response);
    }

    /**
     * 使用手机号登录
     *
     * @param phoneNum
     * @param phoneCaptcha
     * @param request
     * @param response
     * @return
     */
    @Override
    public LoginUserVO userLoginBySms(String phoneNum, String phoneCaptcha, HttpServletRequest request, HttpServletResponse response) {
        boolean verifyCode = smsManager.verifyCode(phoneNum, phoneCaptcha);
        if (!verifyCode) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机验证码错误！");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phoneNum", phoneNum);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在！");
        }
        // 3. 记录用户的登录态
        return setLoginUser(user, response);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Long userId = JwtUtils.getUserIdByToken(request);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        String userJson = redisTemplate.opsForValue().get(USER_LOGIN_STATE + userId);
        User currentUser = GSON.fromJson(userJson, User.class);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookieItem : cookies) {
            if (CookieConstant.TOKEN_KEY.equals(cookieItem.getName())) {
                Long userId = JwtUtils.getUserIdByToken(request);
                ThrowUtils.throwIf(userId == null, ErrorCode.OPERATION_ERROR, "未登录");
                // 移除登录态
                redisTemplate.delete(USER_LOGIN_STATE + userId);
                Cookie timeOutCookie = new Cookie(cookieItem.getName(), cookieItem.getValue());
                timeOutCookie.setMaxAge(0);
                response.addCookie(timeOutCookie);
                return true;
            }
        }
        return false;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 生成图像验证码
     *
     * @param request
     * @param response
     */
    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        // 前端必须传一个 signature 来作为唯一标识
        String signature = request.getHeader("signature");
        if (StringUtils.isEmpty(signature)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        try {
            // 自定义纯数字的验证码（随机4位数字，可重复）
            RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
            LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 30);
            lineCaptcha.setGenerator(randomGenerator);
            // 设置响应头
            response.setContentType("image/jpeg");
            response.setHeader("Pragma", "No-cache");
            // 输出到页面
            lineCaptcha.write(response.getOutputStream());
            // 打印日志
            log.info("captchaId：{} ----生成的验证码:{}", signature, lineCaptcha.getCode());
            // 将验证码设置到Redis中, 3分钟过期
            redisTemplate.opsForValue().set(RedisConstant.CAPTCHA_PREFIX + signature, lineCaptcha.getCode(), 3, TimeUnit.MINUTES);
            // 关闭流
            response.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送短信验证码
     *
     * @param phoneNum
     * @return
     */
    @Override
    public Boolean sendSmsCaptcha(String phoneNum) {
        if (StringUtils.isEmpty(phoneNum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不能为空");
        }
        // 手机号码格式校验
        boolean checkPhoneNum = Validator.isMobile(phoneNum);
        if (!checkPhoneNum) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        // 生成随机验证码
        int code = Integer.parseInt(RandomUtil.randomNumbers(4));
        SendSmsRequest smsRequest = new SendSmsRequest();
        smsRequest.setPhoneNum(phoneNum);
        smsRequest.setCode(String.valueOf(code));
        return smsManager.sendSms(smsRequest);
    }

    /**
     * 生成当前用户的ak,sk
     *
     * @param request
     * @return
     */
    @Override
    public UserDevKeyVO genkey(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        UserDevKeyVO userDevKeyVO = genKey(loginUser.getUserAccount());
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userAccount", loginUser.getUserAccount());
        updateWrapper.eq("id", loginUser.getId());
        updateWrapper.set("accessKey", userDevKeyVO.getAccessKey());
        updateWrapper.set("secretKey", userDevKeyVO.getSecretKey());
        this.update(updateWrapper);
        loginUser.setAccessKey(userDevKeyVO.getAccessKey());
        loginUser.setSecretKey(userDevKeyVO.getSecretKey());
        request.getSession().setAttribute(USER_LOGIN_STATE, loginUser);
        return userDevKeyVO;
    }

    /**
     * 通过 ak 获取用户
     *
     * @param accessKey
     * @return
     */
    @Override
    public User getUserByAK(String accessKey) {
        if (StringUtils.isAnyBlank(accessKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey", accessKey);
        return userMapper.selectOne(queryWrapper);
    }

    private UserDevKeyVO genKey(String userAccount) {
        String accessKey = DigestUtil.md5Hex(UserConstant.SALT + userAccount + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(UserConstant.SALT + userAccount + RandomUtil.randomNumbers(8));
        UserDevKeyVO userDevKeyVO = new UserDevKeyVO();
        userDevKeyVO.setAccessKey(accessKey);
        userDevKeyVO.setSecretKey(secretKey);
        return userDevKeyVO;
    }

    /**
     * 记录用户的登录态，并返回脱敏后的登录用户
     *
     * @param response
     * @param user
     * @return
     */
    private LoginUserVO setLoginUser(User user, HttpServletResponse response) {
        User safetyUser = getSafetyUser(user);
        // 生成JWT
        String token = JwtUtils.getToken(safetyUser.getId(), safetyUser.getUserName());
        Cookie cookie = new Cookie(TOKEN_KEY, token);
        cookie.setPath("/api");
        response.addCookie(cookie);
        response.addCookie(new Cookie("SameSite", "None"));
        // 用户信息保存到redis
        String userJson = GSON.toJson(safetyUser);
        redisTemplate.opsForValue().set(USER_LOGIN_STATE + safetyUser.getId(), userJson, UserConstant.EXPIRE, TimeUnit.MILLISECONDS);
        return this.getLoginUserVO(safetyUser);
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param originUser
     * @return
     */
    private User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUserName(originUser.getUserName());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setUserAvatar(originUser.getUserAvatar());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhoneNum(originUser.getPhoneNum());
        safetyUser.setAccessKey(originUser.getAccessKey());
        safetyUser.setSecretKey(originUser.getSecretKey());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        return safetyUser;
    }
}




