package com.itheima.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具。
 * 新密码统一用 BCrypt：自带随机盐，且故意算得慢，能有效抵抗彩虹表和暴力破解。
 * 同时兼容数据库里存量的无盐 MD5 哈希，登录成功后由调用方升级成 BCrypt。
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    // BCrypt 哈希固定以 $2a$ / $2b$ / $2y$ 开头，用它来区分存量 MD5
    private static final String BCRYPT_PREFIX = "$2";

    /**
     * 加密明文密码，用于注册和改密
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验明文密码和数据库里的哈希是否匹配
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }
        if (isBcrypt(storedPassword)) {
            return ENCODER.matches(rawPassword, storedPassword);
        }
        // 兼容历史 MD5 数据
        return storedPassword.equalsIgnoreCase(Md5Util.getMD5String(rawPassword));
    }

    /**
     * 是否是 BCrypt 哈希。返回 false 说明这条密码是历史遗留的 MD5，应当升级
     */
    public static boolean isBcrypt(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith(BCRYPT_PREFIX);
    }
}
