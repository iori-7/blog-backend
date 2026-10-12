package com.itheima.interceptors;

import com.itheima.utils.JwtUtil;
import com.itheima.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //令牌验证
        String token = request.getHeader("Authorization");

        //请求头里没带token，直接拒绝。
        //这一步不能省：下面拿null当key去查redis，会抛IllegalArgumentException("non null key required")，
        //被catch打印成"token校验失败"，排查时容易被带偏
        if (!StringUtils.hasText(token)) {
            response.setStatus(401);
            return false;
        }

        //验证token
        try {
            //从redis获取相同的token
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String s = operations.get(token);
            if (s == null) {
                //没有获取到token，抛出异常
                throw new Exception();
            }

            Map<String, Object> claims = JwtUtil.parseToken(token);
            //将得到的claims存入threadLocal中
            ThreadLocalUtil.set(claims);
            //放行
            return true;
        } catch (Exception e) {
            //http响应状态码为401
            response.setStatus(401);
            System.out.println("❌ token校验失败，异常信息："+e.getClass().getSimpleName() + " 消息："+e.getMessage());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清空ThreadLocal中的数据
        ThreadLocalUtil.remove();
    }

}
