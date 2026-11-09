package com.cetc28s.minichatjava.dao;

import com.cetc28s.minichatjava.model.entity.BugEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * bug 单数据访问层
 */
public interface BugDao extends JpaRepository<BugEntity, String> {

    List<BugEntity> findByVersionIdOrderByCreateTimeDesc(String versionId);

    List<BugEntity> findByVersionIdAndStatusIn(String versionId, Collection<Integer> statuses);

    long countByVersionId(String versionId);
}
