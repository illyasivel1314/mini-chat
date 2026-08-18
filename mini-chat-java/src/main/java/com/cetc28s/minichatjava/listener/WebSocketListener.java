package com.cetc28s.minichatjava.listener;

import com.cetc28s.minichatjava.utils.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketListener.class);

    /**
     * 在线用户集合：userId -> sessionId
     */
    private static final ConcurrentHashMap<String, String> onlineUsers = new ConcurrentHashMap<>();

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 用户连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // 从连接头中提取 token 进行认证
        List<String> authHeaders = headerAccessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String token = authHeaders.get(0);
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            try {
                String userId = jwtTokenUtil.extractClaims(token).getSubject();
                if (userId != null) {
                    onlineUsers.put(userId, sessionId);
                    headerAccessor.getSessionAttributes().put("userId", userId);
                    log.info("用户上线: userId={}, sessionId={}", userId, sessionId);

                    // 广播上线通知
                    Map<String, Object> onlineNotice = new HashMap<>();
                    onlineNotice.put("type", "USER_ONLINE");
                    onlineNotice.put("userId", userId);
                    onlineNotice.put("timestamp", System.currentTimeMillis());
                    messagingTemplate.convertAndSend("/topic/online", onlineNotice);
                }
            } catch (Exception e) {
                log.warn("WebSocket 连接认证失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 用户断开事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // 查找并移除离线用户
        for (Map.Entry<String, String> entry : onlineUsers.entrySet()) {
            if (entry.getValue().equals(sessionId)) {
                String userId = entry.getKey();
                onlineUsers.remove(userId);
                log.info("用户下线: userId={}, sessionId={}", userId, sessionId);

                // 广播下线通知
                Map<String, Object> offlineNotice = new HashMap<>();
                offlineNotice.put("type", "USER_OFFLINE");
                offlineNotice.put("userId", userId);
                offlineNotice.put("timestamp", System.currentTimeMillis());
                messagingTemplate.convertAndSend("/topic/online", offlineNotice);
                break;
            }
        }
    }

}
