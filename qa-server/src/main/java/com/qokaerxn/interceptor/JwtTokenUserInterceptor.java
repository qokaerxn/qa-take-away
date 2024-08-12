package com.qokaerxn.interceptor;

import com.qokaerxn.constant.JwtClaimsConstant;
import com.qokaerxn.context.BaseContext;
import com.qokaerxn.properties.JwtProperties;
import com.qokaerxn.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if ((!(handler instanceof HandlerMethod))) {
            return true;
        }
        String token = request.getHeader(jwtProperties.getUserTokenName());
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long id = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户的Id:");
            BaseContext.setCurrentId(id);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }
}
