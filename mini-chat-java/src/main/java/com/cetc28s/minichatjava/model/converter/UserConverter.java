package com.cetc28s.minichatjava.model.converter;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.cetc28s.minichatjava.controller.request.UserRegisterRequest;
import com.cetc28s.minichatjava.model.dto.GroupDetailDto;
import com.cetc28s.minichatjava.model.dto.UserFriendDto;
import com.cetc28s.minichatjava.model.dto.UserGroupDto;
import com.cetc28s.minichatjava.model.dto.UserLoginDto;
import com.cetc28s.minichatjava.model.entity.ChatGroupEntity;
import com.cetc28s.minichatjava.model.entity.UserEntity;
import com.cetc28s.minichatjava.utils.CommonUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConverter {

    UserConverter MAPPER = Mappers.getMapper(UserConverter.class);

    @Mappings({})
    UserLoginDto convertUserLoginDto(UserEntity userEntity);

    @Named("generateUuid")
    default String generateUuid() {
        return CommonUtil.generateUuid();
    }

    @Named("generatePinyin")
    default String generatePinyin(String value) {
        return PinyinUtil.getPinyin(value);
    }

    @Mappings({
            @Mapping(target = "id", expression = "java(MAPPER.generateUuid())"),
            @Mapping(target = "pinyin", source = "name", qualifiedByName = "generatePinyin"),
    })
    UserEntity convertUserEntity(UserRegisterRequest request);

    @Mappings({})
    UserFriendDto convertUserFriendDto(UserEntity userEntity);

    @Mappings({})
    UserGroupDto convertUserGroupDto(ChatGroupEntity chatGroup);


    @Mappings({})
    GroupDetailDto.MemberDetail convertMemberDetail(UserEntity userEntity);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "groupName"),
            @Mapping(target = "creatorId", source = "creatorId"),
            @Mapping(target = "groupAvatar", source = "groupAvatar"),
            @Mapping(target = "announcement", source = "groupNotice"),
            @Mapping(target = "remark", source = "groupRemark"),
    })
    GroupDetailDto convertGroupDetailDto(ChatGroupEntity chatGroup);
}
