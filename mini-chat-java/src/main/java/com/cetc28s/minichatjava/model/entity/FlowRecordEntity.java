package com.cetc28s.minichatjava.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 单据流转记录实体（单实体 + 流转记录：创建 -> 转包1 -> 转包2 ...）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flow_record_table")
public class FlowRecordEntity implements Serializable {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /**
     * 单据类型：0需求单/1bug单
     */
    @Column(name = "ticket_type", nullable = false)
    private int ticketType;

    /**
     * 单据ID
     */
    @Column(name = "ticket_id", nullable = false)
    private String ticketId;

    /**
     * 动作：0创建/1转包
     */
    @Column(name = "action", nullable = false)
    private int action;

    /**
     * 操作人ID
     */
    @Column(name = "operator_id", nullable = false)
    private String operatorId;

    /**
     * 转出人ID（转包时）
     */
    @Column(name = "from_user_id")
    private String fromUserId;

    /**
     * 转入人ID（转包时；创建时为负责人）
     */
    @Column(name = "to_user_id")
    private String toUserId;

    /**
     * 备注（转包必填）
     */
    @Column(name = "remark")
    private String remark;

    /**
     * 操作时间戳（毫秒）
     */
    @Column(name = "create_time", nullable = false)
    private Long createTime;
}
