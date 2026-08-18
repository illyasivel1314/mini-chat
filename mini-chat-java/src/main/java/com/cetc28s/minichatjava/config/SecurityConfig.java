package com.cetc28s.minichatjava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（REST API 不需要）
            .csrf().disable()
            // 无状态会话（使用 JWT）
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 请求授权配置
            .authorizeRequests()
                // 放行登录、注册接口
                .antMatchers("/api/auth/login", "/api/auth/register", "/api/file/upload").permitAll()
                // 放行 WebSocket 端点
                .antMatchers("/ws/**").permitAll()
                // 其他所有请求放行（由 TokenInterceptor 统一处理认证）
                .anyRequest().permitAll()
            .and()
            // 禁用默认的 form login
            .formLogin().disable()
            // 禁用默认的 http basic
            .httpBasic().disable()
            // 禁用默认的 logout
            .logout().disable()
            // 禁用 frameOptions（防止 H2 控制台等问题）
            .headers().frameOptions().disable();

        return http.build();
    }
}
