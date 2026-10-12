package com.itheima.service;

import com.itheima.pojo.User;

public interface UserService {
    //根据用户名查询用户
    User findByUsername(String username);

    //注册账户
    void register(String username, String password);

    //更新用户信息
    void update(User user);

    //更新头像
    void updateAvatar(String avatarUrl);

    //更新密码
    void updatePwd(String password);

    //按用户id更新密码（登录时把历史 MD5 密码升级成 BCrypt，此时还没有 ThreadLocal 上下文）
    void updatePwdById(Integer userId, String rawPassword);
}
