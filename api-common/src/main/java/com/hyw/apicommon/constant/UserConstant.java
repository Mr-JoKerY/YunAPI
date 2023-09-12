package com.hyw.apicommon.constant;

/**
 * 用户常量
 *
 * @author hyw
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "userLoginState";

    /**
     * 系统用户 id（虚拟用户）
     */
    long SYSTEM_USER_ID = 0;

    //  region 权限

    /**
     * 默认权限
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员权限
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion

    // 盐值，混淆密码
    String SALT = "test";

    int USERACCOUNT_MINLENGTH = 4;

    int USERACCOUNT_MAXLENGTH = 13;

    int USERNAME_LENGTH = 7;

    int USERPASSWORD_LENGTH = 8;

    long EXPIRE = 1000 * 60 * 60 * 24; // token过期时间
}
