package com.cetc28s.minichatjava.controller;

import com.cetc28s.minichatjava.controller.request.*;
import com.cetc28s.minichatjava.model.context.UserContext;
import com.cetc28s.minichatjava.model.entity.BugEntity;
import com.cetc28s.minichatjava.model.entity.FlowRecordEntity;
import com.cetc28s.minichatjava.model.entity.RequirementEntity;
import com.cetc28s.minichatjava.model.entity.VersionEntity;
import com.cetc28s.minichatjava.service.TaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 任务协同控制层（F08/F09/F10）
 */
@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Resource
    private TaskService taskService;

    // ==================== 版本 ====================

    @PostMapping("/version/create")
    public VersionEntity createVersion(@Validated @RequestBody VersionCreateRequest request) {
        return taskService.createVersion(UserContext.getCurrentUser(), request);
    }

    @GetMapping("/version/list")
    public List<VersionEntity> listVersions() {
        return taskService.listVersions();
    }

    @PostMapping("/version/status")
    public VersionEntity updateVersionStatus(@Validated @RequestBody VersionStatusRequest request) {
        return taskService.updateVersionStatus(UserContext.getCurrentUser(), request);
    }

    @PostMapping("/version/delete")
    public void deleteVersion(@RequestParam String versionId) {
        taskService.deleteVersion(UserContext.getCurrentUser(), versionId);
    }

    // ==================== 需求单 ====================

    @PostMapping("/requirement/create")
    public RequirementEntity createRequirement(@Validated @RequestBody RequirementCreateRequest request) {
        return taskService.createRequirement(UserContext.getCurrentUser(), request);
    }

    @PostMapping("/requirement/update")
    public RequirementEntity updateRequirement(@Validated @RequestBody RequirementUpdateRequest request) {
        return taskService.updateRequirement(UserContext.getCurrentUser(), request);
    }

    @GetMapping("/requirement/list")
    public List<RequirementEntity> listRequirements(@RequestParam String versionId) {
        return taskService.listRequirements(versionId);
    }

    // ==================== bug 单 ====================

    @PostMapping("/bug/create")
    public BugEntity createBug(@Validated @RequestBody BugCreateRequest request) {
        return taskService.createBug(UserContext.getCurrentUser(), request);
    }

    @PostMapping("/bug/update")
    public BugEntity updateBug(@Validated @RequestBody BugUpdateRequest request) {
        return taskService.updateBug(UserContext.getCurrentUser(), request);
    }

    @GetMapping("/bug/list")
    public List<BugEntity> listBugs(@RequestParam String versionId) {
        return taskService.listBugs(versionId);
    }

    // ==================== 转包 / 状态 / 流转 ====================

    @PostMapping("/ticket/transfer")
    public void transferTicket(@Validated @RequestBody TicketTransferRequest request) {
        taskService.transferTicket(UserContext.getCurrentUser(), request);
    }

    @PostMapping("/ticket/status")
    public void updateTicketStatus(@Validated @RequestBody TicketStatusRequest request) {
        taskService.updateTicketStatus(UserContext.getCurrentUser(), request);
    }

    @GetMapping("/ticket/flow")
    public List<FlowRecordEntity> listFlowRecords(@RequestParam int ticketType, @RequestParam String ticketId) {
        return taskService.listFlowRecords(ticketType, ticketId);
    }
}
