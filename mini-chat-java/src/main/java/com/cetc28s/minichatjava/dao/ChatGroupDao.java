package com.cetc28s.minichatjava.dao;


import com.cetc28s.minichatjava.model.entity.ChatGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ChatGroupDao extends JpaRepository<ChatGroupEntity, String> {

    List<ChatGroupEntity> findAllByIdIn(Collection<String> ids);
}
