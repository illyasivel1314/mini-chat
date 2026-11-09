package com.cetc28s.minichatjava.interrupt;

import com.cetc28s.minichatjava.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * admin 后台鉴权拦截器：独立 admin 账号，与普通用户会话隔离
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Value("${admin.username}")
    private String adminUsername;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.toString().equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || !jwtTokenUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or missing token");
            return false;
        }
        String subject = jwtTokenUtil.extractClaims(token).getSubject();
        if (!adminUsername.equals(subject)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Admin access only");
            return false;
        }
        return true;
    }
}
