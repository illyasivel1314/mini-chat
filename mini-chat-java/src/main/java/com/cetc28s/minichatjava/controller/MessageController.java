package com.cetc28s.minichatjava.controller;

import com.cetc28s.minichatjava.dao.UserDao;
import com.cetc28s.minichatjava.model.converter.MessageConverter;
import com.cetc28s.minichatjava.model.dto.ChatMessageDto;
import com.cetc28s.minichatjava.model.entity.UserEntity;
import com.cetc28s.minichatjava.model.enums.ChatTypeEnum;
import com.cetc28s.minichatjava.model.vo.SendMessageVo;
import com.cetc28s.minichatjava.service.ConversationService;
import com.cetc28s.minichatjava.service.MessageService;
import com.cetc28s.minichatjava.service.UserService;
import com.cetc28s.minichatjava.utils.CommonUtil;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.Map;

@Controller
public class MessageController {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    @Resource
    private ConversationService conversationService;


    // ==================== WebSocket 消息处理 ====================
    // /topic：广播消息代理，所有订阅者都能收到
    // /queue：点对点消息代理
    // /app：客户端发送消息到服务端的统一前缀
    // /user：私聊消息的前缀，Spring 会自动解析为对应用户的会话

    /**
     * 处理聊天消息发送
     * 客户端发送到 /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload SendMessageVo message, Principal principal) {
        if (principal == null || message == null) {
            return;
        }
        String currentUserId = principal.getName();
        // 将数据进行打包
        ChatMessageDto chatMessageDto = MessageConverter.MAPPER.convertChatMessageDto(message, currentUserId);
        // 会话ID（sessionWindowId）
        String conversationId = conversationService.resolveConversationId(
                message.getChatType(), currentUserId, message.getReceiverId());
        chatMessageDto.setConversationId(conversationId);
        // 保存消息到数据库
        messageService.saveAndBuildMessage(chatMessageDto);
        // 维护会话与未读/@角标
        conversationService.onMessageSent(message.getChatType(), conversationId,
                currentUserId, message.getReceiverId(), message.getAtUsers(),
                Boolean.TRUE.equals(message.getAtAll()));
        // 获取数据类型
        if (ChatTypeEnum.PRIVATE.getValue() == message.getChatType()) {
            // 私聊：发送给接收者
            messagingTemplate.convertAndSendToUser(message.getReceiverId(), "/queue/private", chatMessageDto);
            // 同时发送给发送者（确认消息已发送）
            messagingTemplate.convertAndSendToUser(currentUserId, "/queue/private", chatMessageDto);
        } else if (ChatTypeEnum.GROUP.getValue() == message.getChatType()) {
            // 群聊：获取会话内全部成员，逐个私聊推送（前端按 conversationId 归属会话）
            for (String memberId : conversationService.listGroupMemberIds(message.getReceiverId())) {
                messagingTemplate.convertAndSendToUser(memberId, "/queue/private", chatMessageDto);
            }
        } else if (ChatTypeEnum.SELF.getValue() == message.getChatType()) {
            // 自发会话：仅回显给发送者
            messagingTemplate.convertAndSendToUser(currentUserId, "/queue/private", chatMessageDto);
        }
    }

    /**
     * 心跳处理：客户端发送到 /app/chat.ping，服务端回 /queue/pong
     */
    @MessageMapping("/chat.ping")
    public void handlePing(@Payload Map<String, Object> ping, Principal principal) {
        if (principal == null) {
            return;
        }
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/pong", "pong");
    }
}
