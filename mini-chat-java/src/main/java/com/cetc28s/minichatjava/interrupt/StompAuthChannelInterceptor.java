package com.cetc28s.minichatjava.interrupt;

import com.cetc28s.minichatjava.utils.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.List;

/**
 * STOMP 通道拦截器 —— 处理 WebSocket 连接的 JWT 认证
 * 在 STOMP CONNECT 阶段提取 token 并设置 Principal
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompAuthChannelInterceptor.class);

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 只处理 CONNECT 帧
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            String token = null;

            if (authHeaders != null && !authHeaders.isEmpty()) {
                token = authHeaders.get(0);
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
            }

            if (token != null) {
                try {
                    String userId = jwtTokenUtil.extractClaims(token).getSubject();
                    if (userId != null) {
                        // 设置 Principal，后续 @MessageMapping 方法可以直接通过 Principal 获取
                        final String finalUserId = userId;
                        accessor.setUser(new Principal() {
                            @Override
                            public String getName() {
                                return finalUserId;
                            }
                        });
                        log.info("WebSocket 认证成功: userId={}", userId);
                    }
                } catch (Exception e) {
                    log.warn("WebSocket 认证失败: {}", e.getMessage());
                    // 认证失败不阻断连接，由业务层处理匿名消息
                }
            }
        }
        return message;
    }
}
