package com.cetc28s.minichatjava.interrupt;

import com.cetc28s.minichatjava.model.context.UserContext;
import com.cetc28s.minichatjava.utils.JwtTokenUtil;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 如果是 OPTIONS 请求，直接放行，不进行 Token 验证
        if (HttpMethod.OPTIONS.toString().equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 从请求头中获取 Token，通常使用 "Authorization" 头[reference:11][reference:12]
        String token = request.getHeader("Authorization");

        // 2. 简单的格式检查，例如 Bearer token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 3. 验证 Token
        if (token == null || !jwtTokenUtil.validateToken(token)) {
            // 验证失败，返回401 Unauthorized状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // 也可以返回自定义的错误信息
            response.getWriter().write("Invalid or missing token");
            return false; // 拦截请求，不再继续执行
        }

        // 4. 验证通过，从Token中解析用户信息
        String username = jwtTokenUtil.extractClaims(token).getSubject();

        // 5. 将用户信息存入 ThreadLocal，供后续业务使用[reference:13]
        UserContext.setCurrentUser(username);

        // 6. 放行请求
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求处理完成后，务必清理 ThreadLocal[reference:14]
        UserContext.clear();
    }
}