package com.cetc28s.minichatjava.service;

import com.cetc28s.minichatjava.controller.request.*;
import com.cetc28s.minichatjava.dao.ChatGroupDao;
import com.cetc28s.minichatjava.dao.GroupMemberDao;
import com.cetc28s.minichatjava.dao.MessageDao;
import com.cetc28s.minichatjava.dao.UserDao;
import com.cetc28s.minichatjava.exception.BusinessException;
import com.cetc28s.minichatjava.exception.ErrorCodeEnum;
import com.cetc28s.minichatjava.model.converter.UserConverter;
import com.cetc28s.minichatjava.model.dto.GroupDetailDto;
import com.cetc28s.minichatjava.model.dto.UserFriendDto;
import com.cetc28s.minichatjava.model.dto.UserGroupDto;
import com.cetc28s.minichatjava.model.dto.UserLoginDto;
import com.cetc28s.minichatjava.model.entity.ChatGroupEntity;
import com.cetc28s.minichatjava.model.entity.GroupMemberEntity;
import com.cetc28s.minichatjava.model.entity.MessageEntity;
import com.cetc28s.minichatjava.model.entity.UserEntity;
import com.cetc28s.minichatjava.model.enums.ChatTypeEnum;
import com.cetc28s.minichatjava.model.enums.ContentTypeEnum;
import com.cetc28s.minichatjava.model.vo.MessageTextVo;
import com.cetc28s.minichatjava.utils.CommonUtil;
import com.cetc28s.minichatjava.utils.JwtTokenUtil;
import com.google.gson.Gson;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Resource
    private UserDao userDao;

    @Resource
    private ChatGroupDao chatGroupDao;

    @Resource
    private GroupMemberDao groupMemberDao;

    @Resource
    private MessageDao messageDao;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 用户登陆
     * @param request 请求
     * @return 用户登录信息（含token）
     */
    public UserLoginDto login(UserLoginRequest request) {
        String phone = request.getPhone();
        String password = request.getPassword();
        // 根据手机号查询用户
        UserEntity user = userDao.findByPhone(phone);
        if (Objects.isNull(user)) {
            throw new BusinessException(ErrorCodeEnum.USER_NOT_FOUND);
        }
        // 逻辑删除账号无法登录
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BusinessException(ErrorCodeEnum.ACCOUNT_DELETED);
        }
        // 验证密码
        if (!user.getPassword().equals(password)) {
            throw new BusinessException(ErrorCodeEnum.INVALID_PASSWORD);
        }

        // 转化
        UserLoginDto userLoginDto = UserConverter.MAPPER.convertUserLoginDto(user);
        userLoginDto.setToken(jwtTokenUtil.generateToken(user.getId()));
        return userLoginDto;
    }

    /**
     * 用户注册（注册成功后自动登录，返回用户信息与 token）
     * @param request 注册请求
     * @return 用户登录信息（含 token）
     */
    public UserLoginDto register(UserRegisterRequest request) {
        // 判断手机号是否重复
        if (userDao.existsByPhone(request.getPhone())) {
            throw new BusinessException(ErrorCodeEnum.DUPLICATE_PHONE);
        }

        // 构建用户实体
        UserEntity userEntity = UserConverter.MAPPER.convertUserEntity(request);
        // 保存到数据库
        userDao.save(userEntity);

        // 注册后自动登录：生成 token
        UserLoginDto userLoginDto = UserConverter.MAPPER.convertUserLoginDto(userEntity);
        userLoginDto.setToken(jwtTokenUtil.generateToken(userEntity.getId()));
        return userLoginDto;
    }

    /**
     * 获取所有的好友
     * @return 好友
     */
    public List<UserFriendDto> acquireAllFriend() {
        return userDao.findAll()
                .stream()
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .map(UserConverter.MAPPER::convertUserFriendDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有加入的群聊
     * @return List<UserGroupDto>
     */
    public List<UserGroupDto> acquireAllGroup(String currentUserId) {
        // 先查当前用户加入的群成员关系，再查对应群信息
        List<GroupMemberEntity> members = groupMemberDao.findByUserId(currentUserId);
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        List<String> groupIds = members.stream()
                .map(GroupMemberEntity::getGroupId)
                .distinct()
                .collect(Collectors.toList());
        return chatGroupDao.findAllByIdIn(groupIds)
                .stream().map(UserConverter.MAPPER::convertUserGroupDto)
                .collect(Collectors.toList());
    }

    /**
     * 查询指定的用户
     * @param currentUserId 用户id
     * @return 用户
     */
    public UserEntity acquireUserEntity(String currentUserId) {
        return userDao.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.USER_NOT_FOUND));
    }


    /**
     * 创建群聊
     * @param groupCreateRequest 创建请求
     */
    @Transactional
    public void createGroup(String currentUserId, GroupCreateRequest groupCreateRequest) {
        // 创建群聊
        ChatGroupEntity chatGroupEntity = ChatGroupEntity
                .builder()
                .id(CommonUtil.generateUuid())
                .groupName(groupCreateRequest.getName())
                .creatorId(currentUserId)
                .ownerId(currentUserId)
                .groupAvatar(groupCreateRequest.getAvatar())
                .createTime(CommonUtil.generateTimestamp())
                .build();
        chatGroupDao.save(chatGroupEntity);

        // 加入好友
        List<GroupMemberEntity> groupMemberEntityeList = groupCreateRequest.getUserIdList()
                .stream()
                .map(id -> GroupMemberEntity.builder()
                        .id(CommonUtil.generateUuid())
                        .groupId(chatGroupEntity.getId())
                        .userId(id)
                        .joinTime(CommonUtil.generateTimestamp())
                        .build()
                )
                .collect(Collectors.toList());
        groupMemberDao.saveAll(groupMemberEntityeList);

        // 添加系统消息
        Gson gson = new Gson();
        MessageEntity messageEntity = MessageEntity.builder()
                .id(CommonUtil.generateUuid())
                .senderId("")
                .receiverId(chatGroupEntity.getId())
                .chatType(ChatTypeEnum.SYSTEM.getValue())
                .contentType(ContentTypeEnum.TEXT.getValue())
                .content(gson.toJson(new MessageTextVo("创建了" + groupCreateRequest.getName() + "群聊")))
                .timestamp(CommonUtil.generateTimestamp())
                .isRead(false)
                .build();
        messageDao.save(messageEntity);
    }

    /**
     * 获取群聊明细
     * @param groupId 群聊id
     */
    public GroupDetailDto acquireGroupDetail(String groupId) {
        ChatGroupEntity chatGroupEntity = chatGroupDao.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.USER_NOT_FOUND));
        GroupDetailDto groupDetailDto = UserConverter.MAPPER.convertGroupDetailDto(chatGroupEntity);
        // 查询群成员
        List<GroupMemberEntity> groupMemberEntityList = groupMemberDao.findByGroupId(groupId);
        if (!CollectionUtils.isEmpty(groupMemberEntityList)) {
            List<String> userIdList = groupMemberEntityList.stream().map(GroupMemberEntity::getUserId).collect(Collectors.toList());
            List<UserEntity> userEntityList = userDao.findAllByIdIn(userIdList);
            groupDetailDto.setMembers(userEntityList.stream().map(UserConverter.MAPPER::convertMemberDetail).collect(Collectors.toList()));
        }
        return groupDetailDto;
    }

    /**
     * 获取群聊成员列表
     * @param groupId 群聊id
     * @return 群成员列表
     */
    public List<UserFriendDto> acquireGroupMembers(String groupId) {
        List<GroupMemberEntity> members = groupMemberDao.findByGroupId(groupId);
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        List<String> userIds = members.stream()
                .map(GroupMemberEntity::getUserId)
                .collect(Collectors.toList());
        return userDao.findAllByIdIn(userIds)
                .stream()
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .map(UserConverter.MAPPER::convertUserFriendDto)
                .collect(Collectors.toList());
    }


    /**
     * 添加好友加入群聊
     * @param groupAddRequest 添加好友
     */
    public void addGroupMembers(GroupAddRequest groupAddRequest) {
        // 加入好友
        List<GroupMemberEntity> groupMemberEntityeList = groupAddRequest.getUserIdList()
                .stream()
                .map(id -> GroupMemberEntity.builder()
                        .id(CommonUtil.generateUuid())
                        .groupId(groupAddRequest.getGroupId())
                        .userId(id)
                        .joinTime(CommonUtil.generateTimestamp())
                        .build()
                )
                .collect(Collectors.toList());
        groupMemberDao.saveAll(groupMemberEntityeList);
    }

    /**
     * 更新
     */
    public void updateGroupDetail(GroupDetailRequest groupDetailRequest) {
        ChatGroupEntity chatGroupEntity = chatGroupDao.findById(groupDetailRequest.getGroupId())
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.USER_NOT_FOUND));;
        if (StringUtils.hasLength(groupDetailRequest.getName())) {
            chatGroupEntity.setGroupName(groupDetailRequest.getName());
        }
        if (StringUtils.hasLength(groupDetailRequest.getAnnouncement())) {
            chatGroupEntity.setGroupNotice(groupDetailRequest.getAnnouncement());
        }
        if (StringUtils.hasLength(groupDetailRequest.getRemark())) {
            chatGroupEntity.setGroupRemark(groupDetailRequest.getRemark());
        }
        chatGroupDao.save(chatGroupEntity);
    }

    /**
     * 离开
     */
    public void leaveGroup(String groupId, String userId) {
        groupMemberDao.deleteByGroupIdAndUserId(groupId, userId);
    }

}
