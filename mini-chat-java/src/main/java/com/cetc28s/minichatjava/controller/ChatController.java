package com.cetc28s.minichatjava.controller;

import com.cetc28s.minichatjava.model.context.UserContext;
import com.cetc28s.minichatjava.model.dto.ChatFriendDto;
import com.cetc28s.minichatjava.model.dto.ChatMessageDto;
import com.cetc28s.minichatjava.service.MessageService;
import com.cetc28s.minichatjava.utils.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Resource
    private MessageService messageService;

    /**
     * 获取私聊历史消息
     */
    @GetMapping("private")
    public Slice<ChatMessageDto> getPrivateHistory(
            @RequestParam String userId,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        String currentUserId = UserContext.getCurrentUser();
        return messageService.getPrivateHistory(currentUserId, userId, pageable);
    }

    /**
     * 获取群聊历史消息
     */
    @GetMapping("group")
    public Slice<ChatMessageDto> getGroupHistory(
            @RequestParam String groupId,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return messageService.getGroupHistory(groupId, pageable);
    }

    /**
     * 获取有聊天记录的好友列表
     */
    @PostMapping("message/friend")
    public List<ChatFriendDto> getChatMessageFriend() {
        // 当前用户id
        String currentUserId = UserContext.getCurrentUser();
        return messageService.getChatMessageFriend(currentUserId);
    }

    /**
     * 标记消息为已读
     * @param userId 发送方
     */
    @GetMapping("/message/read")
    public void markAsRead(@RequestParam String userId) {
        // 当前用户id
        String currentUserId = UserContext.getCurrentUser();
        messageService.markAsRead(currentUserId, userId);
    }
}
