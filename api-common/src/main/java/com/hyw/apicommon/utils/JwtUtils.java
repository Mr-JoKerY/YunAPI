package com.hyw.apicommon.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.hyw.apicommon.constant.CookieConstant;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;

import static com.hyw.apicommon.constant.CookieConstant.*;

/**
 * @author hyw
 */
public class JwtUtils {

    // 生成token字符串
    public static String getToken(Long id, String userName) {
        String token = JWT.create()
                .addPayloads(new HashMap() {{
                    put(COOKIE_USERID_KEY, id);
                    put(COOKIE_USERNAME_KEY, userName);
                }})
                .setSigner(JWTSignerUtil.hs256(APP_SECRET.getBytes()))
                .setExpiresAt(DateUtil.offsetDay(new Date(), 30)).sign();
        return token;
    }

    /**
     * 判断token是否存在与有效
     *
     * @param token
     * @return
     */
    public static boolean checkToken(String token) {
        return JWTUtil.verify(token, JWTSignerUtil.hs256(APP_SECRET.getBytes()));
    }

    /**
     * 获取token字符串中有效载荷部分的数据(有效载荷部分包含用户的数据)
     *
     * @param request
     * @return
     */
    public static Long getUserIdByToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        for (Cookie cookieItem : cookies) {
            if (CookieConstant.TOKEN_KEY.equals(cookieItem.getName())) {
                token = cookieItem.getValue();
                break;
            }
        }
        // JWT验证
        if (StringUtils.isBlank(token) || !JWTUtil.verify(token, JWTSignerUtil.hs256(APP_SECRET.getBytes()))) {
            return null;
        }
        // JWT解析
        JWT jwt = JWTUtil.parseToken(token);
        Object claim = jwt.getPayload().getClaim(COOKIE_USERID_KEY);
        Long userId = null;
        if (claim != null) {
            userId = Long.parseLong(claim.toString());
        }
        return userId;
    }
}