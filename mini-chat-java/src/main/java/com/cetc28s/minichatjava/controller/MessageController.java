package com.cetc28s.minichatjava.controller;

import com.cetc28s.minichatjava.dao.UserDao;
import com.cetc28s.minichatjava.model.converter.MessageConverter;
import com.cetc28s.minichatjava.model.dto.ChatMessageDto;
import com.cetc28s.minichatjava.model.entity.UserEntity;
import com.cetc28s.minichatjava.model.enums.ChatTypeEnum;
import com.cetc28s.minichatjava.model.vo.SendMessageVo;
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
        // 保存消息到数据库
        messageService.saveAndBuildMessage(chatMessageDto);
        // 获取数据类型
        if (ChatTypeEnum.PRIVATE.getValue() == message.getChatType()) {
            // 私聊：发送给接收者
            messagingTemplate.convertAndSendToUser(message.getReceiverId(), "/queue/private", chatMessageDto);
            // 同时发送给发送者（确认消息已发送）
            messagingTemplate.convertAndSendToUser(currentUserId, "/queue/private", chatMessageDto);
        } else if (ChatTypeEnum.GROUP.getValue() == message.getChatType()) {
            // 群聊：广播到群组 topic
            messagingTemplate.convertAndSend("/topic/group", chatMessageDto);
        }
    }
}
