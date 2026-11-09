package com.cetc28s.minichatjava.dao;

import com.cetc28s.minichatjava.model.entity.ConversationMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 会话成员与已读位点数据访问层
 */
public interface ConversationMemberDao extends JpaRepository<ConversationMemberEntity, String> {

    /**
     * 查询某用户在某会话的成员记录
     */
    ConversationMemberEntity findByConversationIdAndUserId(String conversationId, String userId);

    /**
     * 查询某会话的全部成员记录
     */
    List<ConversationMemberEntity> findByConversationId(String conversationId);
}
