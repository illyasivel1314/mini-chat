package com.cetc28s.minichatjava.dao;

import com.cetc28s.minichatjava.model.entity.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupMemberDao extends JpaRepository<GroupMemberEntity, String> {

    /**
     * 获取当前用户加入了哪些群聊
     * @param userId 当前用户id
     * @return 群聊信息列表
     */
    List<GroupMemberEntity> findByUserId(String userId);

    int countByGroupId(String groupId);

    List<GroupMemberEntity> findByGroupId(String groupId);

    void deleteByGroupIdAndUserId(String groupId, String userId);
}
