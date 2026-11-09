package com.cetc28s.minichatjava.dao;

import com.cetc28s.minichatjava.model.entity.RequirementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 需求单数据访问层
 */
public interface RequirementDao extends JpaRepository<RequirementEntity, String> {

    List<RequirementEntity> findByVersionIdOrderByCreateTimeDesc(String versionId);

    List<RequirementEntity> findByVersionIdAndStatusIn(String versionId, Collection<Integer> statuses);

    long countByVersionId(String versionId);
}
