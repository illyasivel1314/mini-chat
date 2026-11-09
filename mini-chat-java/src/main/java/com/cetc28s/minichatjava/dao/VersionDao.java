package com.cetc28s.minichatjava.dao;

import com.cetc28s.minichatjava.model.entity.VersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 版本数据访问层
 */
public interface VersionDao extends JpaRepository<VersionEntity, String> {

    List<VersionEntity> findAllByOrderByCreateTimeDesc();
}
