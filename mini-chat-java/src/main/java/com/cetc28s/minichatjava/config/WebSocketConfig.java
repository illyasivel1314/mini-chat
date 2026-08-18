package com.cetc28s.minichatjava.config;

import com.cetc28s.minichatjava.interrupt.StompAuthChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import javax.annotation.Resource;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Resource
    private StompAuthChannelInterceptor stompAuthChannelInterceptor;

    /**
     * 注册 STOMP 端点，客户端通过此处连接
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 WebSocket 端点，客户端通过 /ws 连接
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // 提供降级方案
    }

    /**
     * 配置消息代理和前缀
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用 /topic 广播前缀、/queue 点对点前缀，配置心跳（服务端每10秒发送心跳）
        registry.enableSimpleBroker("/topic", "/queue", "/head")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(taskScheduler());
        // 客户端发送消息的前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 用户订阅前缀（点对点私聊使用）
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册 STOMP 通道拦截器（JWT 认证）
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }

    /**
     * 配置 WebSocket 传输层
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
                .setSendTimeLimit(15 * 1000)
                .setSendBufferSizeLimit(512 * 1024)
                .setMessageSizeLimit(128 * 1024);
    }

    private TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }
}
