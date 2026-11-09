package com.cetc28s.minichatjava.dao;

import com.cetc28s.minichatjava.model.entity.FlowRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 单据流转记录数据访问层
 */
public interface FlowRecordDao extends JpaRepository<FlowRecordEntity, String> {

    List<FlowRecordEntity> findByTicketTypeAndTicketIdOrderByCreateTimeAsc(int ticketType, String ticketId);
}
