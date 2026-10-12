package com.itheima.controller;

import com.itheima.pojo.Result;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utils.JwtUtil;
import com.itheima.utils.PasswordUtil;
import com.itheima.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Validated
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;


    //注册
    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$")String username,@Pattern(regexp = "^\\S{5,16}$") String password){
        if(userService.findByUsername(username)!=null){
            return Result.error("该用户名已被占用");
        }
        userService.register(username,password);
        return Result.success();
    }

    //登录
    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$")String username, @Pattern(regexp = "^\\S{5,16}$")String password){
        //根据用户名查询用户
        User loginUser = userService.findByUsername(username);
        //判断该用户是否存在
        if(loginUser==null){
            return Result.error("用户名错误");
        }
        //判断密码是否正确
        if(PasswordUtil.matches(password, loginUser.getPassword())){
            //历史遗留的 MD5 密码：登录成功后顺手升级成 BCrypt
            if(!PasswordUtil.isBcrypt(loginUser.getPassword())){
                userService.updatePwdById(loginUser.getId(), password);
            }
            //登陆成功
            //生成JWT令牌
            Map<String,Object> claims = new HashMap<>();
            claims.put("id",loginUser.getId());
            claims.put("username",loginUser.getUsername());
            String token = JwtUtil.genToken(claims);
            //把令牌存进redis
            ValueOperations operations = redisTemplate.opsForValue();
            operations.set(token,token, 30,TimeUnit.DAYS);
            return Result.success(token);
        }
        return Result.error("密码错误");
    }

    //获取用户信息
    @GetMapping("/userInfo")
    public Result<User> userInfo(/*@RequestHeader(name="Authorization")String token*/){
        /*Map<String, Object> map = JwtUtil.parseToken(token);
        String  username = (String) map.get("username");*/

        Map<String,Object> map =ThreadLocalUtil.get();
        String username = (String) map.get("username");

        User user = userService.findByUsername(username);
        return Result.success(user);
    }

    //更改用户信息
    @PutMapping("/update")
    public Result update(@Validated @RequestBody User user){
        userService.update(user);
        return Result.success();
    }

    //更新用户头像
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl){
        userService.updateAvatar(avatarUrl);
        return Result.success();
    }

    //更新用户密码
    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map <String,String> params,@RequestHeader("Authorization")  String token){
        //1、校验参数
        String old_pwd = params.get("old_pwd");
        String new_pwd = params.get("new_pwd");
        String re_pwd = params.get("re_pwd");
        if(!StringUtils.hasLength(old_pwd)||!StringUtils.hasLength(new_pwd)||!StringUtils.hasLength(re_pwd)){
            return Result.error("缺少必要的参数");
        }
        //原密码是否正确
        //调用userService根据用户名拿到密码，再和old_pwd比对
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User loginUser = userService.findByUsername(username);
        if(!PasswordUtil.matches(old_pwd, loginUser.getPassword())){
            return Result.error("原密码填写不正确");
        }

        //new_pwd和re_pwd是否一样
        if(!new_pwd.equals(re_pwd)){
            return Result.error("两次填写的新密码不一样");
        }
        //2、调用Service响应密码更新
        userService.updatePwd(new_pwd);
        //进行删除token
        // ！！！如果多次登录，那么每个token都可以用！！！后期修改
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.getOperations().delete(token);
        return Result.success();
    }

}
