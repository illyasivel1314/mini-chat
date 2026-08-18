package com.cetc28s.minichatjava.dao;

import com.cetc28s.minichatjava.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserDao extends JpaRepository<UserEntity, String> {

    // 通过账号与密码查询对应的用户
    UserEntity findByAccountAndPassword(String account, String password);

    // 通过账号查询用户（用于注册时判断工号是否重复）
    UserEntity findByAccount(String account);

    // 通过ID查询用户
    List<UserEntity> findAllByIdIn(Collection<String> ids);

    // 保存用户
    UserEntity save(UserEntity userEntity);

    // 查询所有用户
    List<UserEntity> findAll();


}
