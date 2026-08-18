package com.cetc28s.minichatjava.config;

import com.cetc28s.minichatjava.interrupt.TokenInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.file.upload-dir}")
    private String uploadDir;

    @Resource
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                // 拦截所有请求
                .addPathPatterns("/api/**")
                // 放行登录、注册等不需要Token的接口
                .excludePathPatterns("/api/auth/login", "/api/auth/register", "/api/file/upload")
                // 放行 WebSocket 端点
                .excludePathPatterns("/ws/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /files/** 的请求映射到 file.upload-dir 目录
        // 注意：路径末尾需要加 "/"
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadDir);
    }
}
