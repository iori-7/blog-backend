package com.itheima.service.impl;



//import ch.qos.logback.core.util.MD5Util;
import com.itheima.utils.PasswordUtil;
import com.itheima.mapper.UserMapper;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        User u =userMapper.findByUserName(username);
        return u;
    }

    @Override
    public void register(String username, String password) {
        //加密处理：BCrypt 自带随机盐
        userMapper.add(username, PasswordUtil.encode(password));
        //添加用户
    }

    @Override
    public void update(User user) {
        // id 一律取自当前登录用户（拦截器放进 ThreadLocal 的），
        // 忽略请求体里传的 id。否则任何人带上 {"id": 别人的id} 就能改他人资料。
        Map<String,Object> map = ThreadLocalUtil.get();
        user.setId((Integer) map.get("id"));

        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id =(Integer) map.get("id");
        userMapper.updateAvatar(avatarUrl,id);
    }

    @Override
    public void updatePwd(String new_pwd) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id =(Integer) map.get("id");
        userMapper.updatePwd(PasswordUtil.encode(new_pwd),id);
    }

    @Override
    public void updatePwdById(Integer userId, String rawPassword) {
        userMapper.updatePwd(PasswordUtil.encode(rawPassword), userId);
    }
}
