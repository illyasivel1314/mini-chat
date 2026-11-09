package com.cetc28s.minichatjava.service;

import com.cetc28s.minichatjava.controller.request.*;
import com.cetc28s.minichatjava.dao.*;
import com.cetc28s.minichatjava.exception.BusinessException;
import com.cetc28s.minichatjava.exception.ErrorCodeEnum;
import com.cetc28s.minichatjava.model.entity.*;
import com.cetc28s.minichatjava.model.enums.*;
import com.cetc28s.minichatjava.utils.CommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 任务协同服务（F08/F09/F10/F11：版本、需求单、bug 单、转包流转、通知）
 */
@Service
public class TaskService {

    @Resource
    private VersionDao versionDao;

    @Resource
    private RequirementDao requirementDao;

    @Resource
    private BugDao bugDao;

    @Resource
    private FlowRecordDao flowRecordDao;

    @Resource
    private UserDao userDao;

    @Resource
    private SystemNotifyService systemNotifyService;

    // ==================== 版本（F08） ====================

    /**
     * 创建版本（仅项目负责人）
     */
    public VersionEntity createVersion(String currentUserId, VersionCreateRequest request) {
        requireDuty(currentUserId, DutyEnum.PROJECT_OWNER);
        long now = CommonUtil.generateTimestamp();
        VersionEntity entity = VersionEntity.builder()
                .id(CommonUtil.generateUuid())
                .name(request.getName())
                .description(request.getDescription())
                .planReleaseTime(request.getPlanReleaseTime())
                .status(VersionStatusEnum.IN_PROGRESS.getValue())
                .creatorId(currentUserId)
                .createTime(now)
                .updateTime(now)
                .build();
        return versionDao.save(entity);
    }

    public List<VersionEntity> listVersions() {
        return versionDao.findAllByOrderByCreateTimeDesc();
    }

    /**
     * 修改版本状态（仅项目负责人；关闭=只读；取消=级联取消未完结单据）
     */
    @Transactional
    public VersionEntity updateVersionStatus(String currentUserId, VersionStatusRequest request) {
        requireDuty(currentUserId, DutyEnum.PROJECT_OWNER);
        VersionEntity version = versionDao.findById(request.getVersionId())
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.VERSION_NOT_FOUND));
        version.setStatus(request.getStatus());
        version.setUpdateTime(CommonUtil.generateTimestamp());
        versionDao.save(version);

        if (request.getStatus() == VersionStatusEnum.CANCELLED.getValue()) {
            cascadeCancelVersion(version.getId());
        }
        // 通知该版本下所有未完结单据的负责人
        notifyVersionOwners(version.getId(), "版本「" + version.getName() + "」"
                + (request.getStatus() == VersionStatusEnum.CANCELLED.getValue() ? "已取消" : "已关闭"));
        return version;
    }

    /**
     * 删除版本（仅项目负责人，且仅当版本下无单据）
     */
    public void deleteVersion(String currentUserId, String versionId) {
        requireDuty(currentUserId, DutyEnum.PROJECT_OWNER);
        if (requirementDao.countByVersionId(versionId) > 0 || bugDao.countByVersionId(versionId) > 0) {
            throw new BusinessException(ErrorCodeEnum.VERSION_HAS_TICKETS);
        }
        versionDao.deleteById(versionId);
    }

    // ==================== 需求单（F09） ====================

    /**
     * 创建需求单（项目负责人/开发员）
     */
    @Transactional
    public RequirementEntity createRequirement(String currentUserId, RequirementCreateRequest request) {
        requireDutyIn(currentUserId, DutyEnum.PROJECT_OWNER, DutyEnum.DEVELOPER);
        VersionEntity version = requireEditableVersion(request.getVersionId());
        long now = CommonUtil.generateTimestamp();
        RequirementEntity entity = RequirementEntity.builder()
                .id(CommonUtil.generateUuid())
                .versionId(version.getId())
                .title(request.getTitle())
                .content(request.getContent())
                .ownerId(request.getOwnerId())
                .creatorId(currentUserId)
                .status(TicketStatusEnum.IN_PROGRESS.getValue())
                .createTime(now)
                .updateTime(now)
                .build();
        requirementDao.save(entity);
        addFlowRecord(TicketTypeEnum.REQUIREMENT, entity.getId(), FlowActionEnum.CREATE,
                currentUserId, null, entity.getOwnerId(), null);
        systemNotifyService.notify(entity.getOwnerId(), "您有一条新需求单待处理：「" + entity.getTitle() + "」");
        return entity;
    }

    /**
     * 编辑需求单内容（仅负责人）
     */
    public RequirementEntity updateRequirement(String currentUserId, RequirementUpdateRequest request) {
        RequirementEntity entity = requirementDao.findById(request.getRequirementId())
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.TICKET_NOT_FOUND));
        requireTicketOwner(entity.getOwnerId(), currentUserId);
        requireEditableVersion(entity.getVersionId());
        requireActiveTicket(entity.getStatus());
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            entity.setTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            entity.setContent(request.getContent());
        }
        entity.setUpdateTime(CommonUtil.generateTimestamp());
        return requirementDao.save(entity);
    }

    public List<RequirementEntity> listRequirements(String versionId) {
        return requirementDao.findByVersionIdOrderByCreateTimeDesc(versionId);
    }

    // ==================== bug 单（F10） ====================

    /**
     * 创建 bug 单（所有角色）
     */
    @Transactional
    public BugEntity createBug(String currentUserId, BugCreateRequest request) {
        VersionEntity version = requireEditableVersion(request.getVersionId());
        long now = CommonUtil.generateTimestamp();
        BugEntity entity = BugEntity.builder()
                .id(CommonUtil.generateUuid())
                .versionId(version.getId())
                .title(request.getTitle())
                .detail(request.getDetail())
                .screenshot(request.getScreenshot())
                .ownerId(request.getOwnerId())
                .creatorId(currentUserId)
                .status(TicketStatusEnum.IN_PROGRESS.getValue())
                .createTime(now)
                .updateTime(now)
                .build();
        bugDao.save(entity);
        addFlowRecord(TicketTypeEnum.BUG, entity.getId(), FlowActionEnum.CREATE,
                currentUserId, null, entity.getOwnerId(), null);
        systemNotifyService.notify(entity.getOwnerId(), "您有一条新 bug 单待处理：「" + entity.getTitle() + "」");
        return entity;
    }

    /**
     * 编辑 bug 单标题/明细/截图（仅创建者）
     */
    public BugEntity updateBug(String currentUserId, BugUpdateRequest request) {
        BugEntity entity = bugDao.findById(request.getBugId())
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.TICKET_NOT_FOUND));
        if (!entity.getCreatorId().equals(currentUserId)) {
            throw new BusinessException(ErrorCodeEnum.PERMISSION_DENIED);
        }
        requireEditableVersion(entity.getVersionId());
        requireActiveTicket(entity.getStatus());
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            entity.setTitle(request.getTitle());
        }
        if (request.getDetail() != null && !request.getDetail().isEmpty()) {
            entity.setDetail(request.getDetail());
        }
        if (request.getScreenshot() != null) {
            entity.setScreenshot(request.getScreenshot());
        }
        entity.setUpdateTime(CommonUtil.generateTimestamp());
        return bugDao.save(entity);
    }

    public List<BugEntity> listBugs(String versionId) {
        return bugDao.findByVersionIdOrderByCreateTimeDesc(versionId);
    }

    // ==================== 转包 / 状态（F09/F10） ====================

    /**
     * 转包：需求单=负责人；bug 单=创建者（重新指定负责人）。写流转记录并通知新负责人。
     */
    @Transactional
    public void transferTicket(String currentUserId, TicketTransferRequest request) {
        if (request.getTicketType() == TicketTypeEnum.REQUIREMENT.getValue()) {
            RequirementEntity entity = requirementDao.findById(request.getTicketId())
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnum.TICKET_NOT_FOUND));
            requireTicketOwner(entity.getOwnerId(), currentUserId);
            requireEditableVersion(entity.getVersionId());
            requireActiveTicket(entity.getStatus());
            String from = entity.getOwnerId();
            entity.setOwnerId(request.getToUserId());
            entity.setUpdateTime(CommonUtil.generateTimestamp());
            requirementDao.save(entity);
            addFlowRecord(TicketTypeEnum.REQUIREMENT, entity.getId(), FlowActionEnum.TRANSFER,
                    currentUserId, from, request.getToUserId(), request.getRemark());
            systemNotifyService.notify(request.getToUserId(), "有需求单转包给您：「" + entity.getTitle() + "」");
        } else {
            BugEntity entity = bugDao.findById(request.getTicketId())
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnum.TICKET_NOT_FOUND));
            if (!entity.getCreatorId().equals(currentUserId)) {
                throw new BusinessException(ErrorCodeEnum.PERMISSION_DENIED);
            }
            requireEditableVersion(entity.getVersionId());
            requireActiveTicket(entity.getStatus());
            String from = entity.getOwnerId();
            entity.setOwnerId(request.getToUserId());
            entity.setUpdateTime(CommonUtil.generateTimestamp());
            bugDao.save(entity);
            addFlowRecord(TicketTypeEnum.BUG, entity.getId(), FlowActionEnum.TRANSFER,
                    currentUserId, from, request.getToUserId(), request.getRemark());
            systemNotifyService.notify(request.getToUserId(), "有 bug 单转包给您：「" + entity.getTitle() + "」");
        }
    }

    /**
     * 修改单据状态（仅负责人；bug 单可同时填原因/方案/结果截图）；通知创建者。
     */
    @Transactional
    public void updateTicketStatus(String currentUserId, TicketStatusRequest request) {
        if (request.getTicketType() == TicketTypeEnum.REQUIREMENT.getValue()) {
            RequirementEntity entity = requirementDao.findById(request.getTicketId())
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnum.TICKET_NOT_FOUND));
            requireTicketOwner(entity.getOwnerId(), currentUserId);
            requireActiveTicket(entity.getStatus());
            entity.setStatus(request.getStatus());
            entity.setUpdateTime(CommonUtil.generateTimestamp());
            requirementDao.save(entity);
            systemNotifyService.notify(entity.getCreatorId(), "您的需求单状态已变更：「" + entity.getTitle() + "」");
        } else {
            BugEntity entity = bugDao.findById(request.getTicketId())
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnum.TICKET_NOT_FOUND));
            requireTicketOwner(entity.getOwnerId(), currentUserId);
            requireActiveTicket(entity.getStatus());
            entity.setStatus(request.getStatus());
            if (request.getReason() != null) {
                entity.setReason(request.getReason());
            }
            if (request.getSolution() != null) {
                entity.setSolution(request.getSolution());
            }
            if (request.getResultScreenshot() != null) {
                entity.setResultScreenshot(request.getResultScreenshot());
            }
            entity.setUpdateTime(CommonUtil.generateTimestamp());
            bugDao.save(entity);
            systemNotifyService.notify(entity.getCreatorId(), "您的 bug 单状态已变更：「" + entity.getTitle() + "」");
        }
    }

    /**
     * 流转记录（创建 → 转包1 → 转包2 ...）
     */
    public List<FlowRecordEntity> listFlowRecords(int ticketType, String ticketId) {
        return flowRecordDao.findByTicketTypeAndTicketIdOrderByCreateTimeAsc(ticketType, ticketId);
    }

    // ==================== 内部辅助 ====================

    private void cascadeCancelVersion(String versionId) {
        List<RequirementEntity> requirements = requirementDao.findByVersionIdAndStatusIn(versionId,
                Collections.singletonList(TicketStatusEnum.IN_PROGRESS.getValue()));
        for (RequirementEntity r : requirements) {
            r.setStatus(TicketStatusEnum.CANCELLED.getValue());
            r.setUpdateTime(CommonUtil.generateTimestamp());
            requirementDao.save(r);
        }
        List<BugEntity> bugs = bugDao.findByVersionIdAndStatusIn(versionId,
                Collections.singletonList(TicketStatusEnum.IN_PROGRESS.getValue()));
        for (BugEntity b : bugs) {
            b.setStatus(TicketStatusEnum.CANCELLED.getValue());
            b.setUpdateTime(CommonUtil.generateTimestamp());
            bugDao.save(b);
        }
    }

    private void notifyVersionOwners(String versionId, String text) {
        Set<String> owners = new LinkedHashSet<>();
        requirementDao.findByVersionIdAndStatusIn(versionId,
                        Collections.singletonList(TicketStatusEnum.IN_PROGRESS.getValue()))
                .forEach(r -> owners.add(r.getOwnerId()));
        bugDao.findByVersionIdAndStatusIn(versionId,
                        Collections.singletonList(TicketStatusEnum.IN_PROGRESS.getValue()))
                .forEach(b -> owners.add(b.getOwnerId()));
        for (String owner : owners) {
            systemNotifyService.notify(owner, text);
        }
    }

    private void addFlowRecord(TicketTypeEnum ticketType, String ticketId, FlowActionEnum action,
                               String operatorId, String fromUserId, String toUserId, String remark) {
        FlowRecordEntity record = FlowRecordEntity.builder()
                .id(CommonUtil.generateUuid())
                .ticketType(ticketType.getValue())
                .ticketId(ticketId)
                .action(action.getValue())
                .operatorId(operatorId)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .remark(remark)
                .createTime(CommonUtil.generateTimestamp())
                .build();
        flowRecordDao.save(record);
    }

    private UserEntity requireUser(String userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.USER_NOT_FOUND));
    }

    private void requireDuty(String userId, DutyEnum duty) {
        UserEntity user = requireUser(userId);
        if (user.getDuty() != duty.getValue()) {
            throw new BusinessException(ErrorCodeEnum.PERMISSION_DENIED);
        }
    }

    private void requireDutyIn(String userId, DutyEnum... duties) {
        UserEntity user = requireUser(userId);
        for (DutyEnum duty : duties) {
            if (user.getDuty() == duty.getValue()) {
                return;
            }
        }
        throw new BusinessException(ErrorCodeEnum.PERMISSION_DENIED);
    }

    private void requireTicketOwner(String ownerId, String currentUserId) {
        if (!ownerId.equals(currentUserId)) {
            throw new BusinessException(ErrorCodeEnum.PERMISSION_DENIED);
        }
    }

    private void requireActiveTicket(int status) {
        if (status != TicketStatusEnum.IN_PROGRESS.getValue()) {
            throw new BusinessException(ErrorCodeEnum.TICKET_NOT_EDITABLE);
        }
    }

    private VersionEntity requireEditableVersion(String versionId) {
        VersionEntity version = versionDao.findById(versionId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.VERSION_NOT_FOUND));
        if (version.getStatus() != VersionStatusEnum.IN_PROGRESS.getValue()) {
            throw new BusinessException(ErrorCodeEnum.TICKET_NOT_EDITABLE);
        }
        return version;
    }
}
