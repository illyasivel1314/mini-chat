package com.cetc28s.minichatjava.dao;

import com.cetc28s.minichatjava.model.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 消息数据访问层
 */
public interface MessageDao extends JpaRepository<MessageEntity, String> {

    /**
     * 查询私聊历史消息（两个用户之间的消息）
     */
    Slice<MessageEntity> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByTimestampDesc(
            String senderId1, String receiverId1, String senderId2, String receiverId2, Pageable pageable);


    /**
     * 查询群聊历史消息
     */
    Slice<MessageEntity> findByReceiverIdOrderByTimestampDesc(String groupId, Pageable pageable);



    /**
     * 查询用户的未读消息数量
     */
    long countByReceiverIdAndIsReadFalse(String receiverId);
    /**
     * 标记消息为已读
     */
    List<MessageEntity> findByReceiverIdAndSenderIdAndIsReadFalse(String receiverId, String senderId);

    /**
     * 获取当前群聊最新的一条消息
     */
    MessageEntity findFirstByReceiverIdOrderByTimestampDesc(String groupId);


    /**
     * 获取当前私聊最新的一条消息
     */
    MessageEntity findFirstBySenderIdAndReceiverIdOrderByTimestampDesc(String senderId, String receiverId);

    /**
     * 获取接收者未读消息的数量
     */
    long countAllBySenderIdAndReceiverIdAndIsReadFalse(String senderId, String receiverId);
}
