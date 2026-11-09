package com.cetc28s.minichatjava.dao;

import com.cetc28s.minichatjava.model.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 会话数据访问层
 */
public interface ConversationDao extends JpaRepository<ConversationEntity, String> {
}
