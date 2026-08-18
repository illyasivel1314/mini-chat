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
 * 群组成员实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "group_member_table")
public class GroupMemberEntity implements Serializable {

    @Id
    private String id;

    /**
     * 群组ID
     */
    @Column(name = "group_id", nullable = false)
    private String groupId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * 加入时间戳（毫秒）
     */
    @Column(name = "join_time", nullable = false)
    private Long joinTime;
}
