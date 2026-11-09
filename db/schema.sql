-- =====================================================================
-- 内部沟通协作平台（聊天 + 任务协同）数据库建表脚本
-- 数据库：MySQL 8.x
-- 依据：PRD《内部沟通协作平台》V1.0（2026-10-16）
-- 约定：
--   1. 主键统一为 VARCHAR(32)（应用层生成 UUID，去除横线，见 CommonUtil.generateUuid）
--   2. 时间戳统一为 BIGINT（毫秒级，见 CommonUtil.generateTimestamp）
--   3. 逻辑删除统一用 TINYINT(1)：0=正常 1=删除
--   4. 表名统一以 _table 结尾（与现有 user_table/message_table 等保持一致）
--   5. 字符集 utf8mb4，存储引擎 InnoDB
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 用户表
-- ---------------------------------------------------------------------
CREATE TABLE `user_table` (
  `id`                   VARCHAR(32)  NOT NULL COMMENT '用户ID（UUID）',
  `phone`                VARCHAR(11)  NOT NULL COMMENT '手机号（11位，登录唯一标识）',
  `password`             VARCHAR(32)  NOT NULL COMMENT '密码（前端MD5后存储，不加盐）',
  `name`                 VARCHAR(20)  NOT NULL COMMENT '姓名（2-20字符）',
  `account`              VARCHAR(20)  DEFAULT NULL COMMENT '工号（可选，≤20字符，不做唯一约束）',
  `pinyin`               VARCHAR(64)  DEFAULT NULL COMMENT '姓名拼音（用于搜索）',
  `avatar`               TEXT DEFAULT NULL COMMENT '头像地址（默认头像）',
  `position`             TINYINT      NOT NULL COMMENT '岗位：0负责人/1Java开发/2前端开发/3C端开发/4三方测试/5内部测试',
  `duty`                 TINYINT      NOT NULL COMMENT '职责：0项目负责人/1开发员/2测试员（任务协同权限依据）',
  `detail`               VARCHAR(500) DEFAULT NULL COMMENT '详细信息（自由文本）',
  `position_description` VARCHAR(255) DEFAULT NULL COMMENT '职位概述（历史字段）',
  `deleted`              TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常/1删除',
  `create_time`          BIGINT       NOT NULL COMMENT '创建时间戳（毫秒）',
  `update_time`          BIGINT       DEFAULT NULL COMMENT '更新时间戳（毫秒）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_name` (`name`),
  KEY `idx_account` (`account`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ---------------------------------------------------------------------
-- 2. 会话表（聊天窗口，sessionWindowId）
-- ---------------------------------------------------------------------
CREATE TABLE `conversation_table` (
  `id`          VARCHAR(32) NOT NULL COMMENT '会话ID（sessionWindowId，全局唯一）',
  `type`        TINYINT     NOT NULL COMMENT '会话类型：0私聊/1群聊/2系统消息/3自发会话(我的文件)',
  `group_id`    VARCHAR(32) DEFAULT NULL COMMENT '群组ID（type=1 时关联 chat_group_table.id）',
  `create_time` BIGINT      NOT NULL COMMENT '创建时间戳（毫秒）',
  `update_time` BIGINT      DEFAULT NULL COMMENT '最近活跃时间戳（毫秒，最新一条消息）',
  PRIMARY KEY (`id`),
  KEY `idx_group` (`group_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会话表（聊天窗口）';

-- ---------------------------------------------------------------------
-- 3. 会话成员与已读位点表
--    PRIVATE/GROUP/SELF/SYSTEM 会话统一在此维护「用户×会话」的成员关系
--    与「本视角已读位点 / 未读计数 / @未读计数」
-- ---------------------------------------------------------------------
CREATE TABLE `conversation_member_table` (
  `id`              VARCHAR(32) NOT NULL COMMENT '主键（UUID）',
  `conversation_id` VARCHAR(32) NOT NULL COMMENT '会话ID',
  `user_id`         VARCHAR(32) NOT NULL COMMENT '用户ID',
  `last_read_time`  BIGINT      DEFAULT NULL COMMENT '本视角已读位点（毫秒时间戳，打开会话即更新）',
  `unread_count`    INT         NOT NULL DEFAULT 0 COMMENT '未读消息数（会话列表角标）',
  `at_count`        INT         NOT NULL DEFAULT 0 COMMENT '未清除的[有人@我]计数（红色角标）',
  `create_time`     BIGINT      NOT NULL COMMENT '加入会话时间戳（毫秒）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conv_user` (`conversation_id`, `user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会话成员与已读位点表';

-- ---------------------------------------------------------------------
-- 4. 聊天消息表
-- ---------------------------------------------------------------------
CREATE TABLE `message_table` (
  `id`              VARCHAR(32) NOT NULL COMMENT '消息ID（UUID）',
  `conversation_id` VARCHAR(32) DEFAULT NULL COMMENT '会话ID（sessionWindowId）',
  `sender_id`       VARCHAR(32) NOT NULL COMMENT '发送者ID（系统消息为空串）',
  `receiver_id`     VARCHAR(32) DEFAULT NULL COMMENT '接收者ID（群聊=群id/私聊=对方id/系统=接收用户id）',
  `chat_type`       TINYINT     NOT NULL COMMENT '聊天类型：0私聊/1群聊/2系统/3自发',
  `content_type`    TINYINT     NOT NULL COMMENT '内容类型：0文本/1图片/2文件/3GIF表情',
  `content`         TEXT        NOT NULL COMMENT '消息内容（JSON Schema，GIF含 emojiKey=分组/文件名）',
  `reply_to`        VARCHAR(32) DEFAULT NULL COMMENT '引用消息ID（引用深度仅1层）',
  `at_users`        TEXT        DEFAULT NULL COMMENT '@提及的用户ID列表（JSON数组）',
  `at_all`          TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否@所有人：0否/1是',
  `forward_from`    VARCHAR(32) DEFAULT NULL COMMENT '转发来源消息ID（单条转发，标记来自XX）',
  `recalled`        TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否撤回：0否/1是',
  `recall_time`     BIGINT      DEFAULT NULL COMMENT '撤回时间戳（毫秒）',
  `is_read`         TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否已读（遗留字段，建议迁移到 conversation_member_table）',
  `timestamp`       BIGINT      NOT NULL COMMENT '发送时间戳（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_conv_ts` (`conversation_id`, `timestamp`),
  KEY `idx_sender` (`sender_id`),
  KEY `idx_receiver` (`receiver_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '聊天消息表';

-- ---------------------------------------------------------------------
-- 5. 群组表
-- ---------------------------------------------------------------------
CREATE TABLE `chat_group_table` (
  `id`           VARCHAR(32)  NOT NULL COMMENT '群组ID',
  `group_name`   VARCHAR(30)  NOT NULL COMMENT '群名（≤30字符）',
  `creator_id`   VARCHAR(32)  NOT NULL COMMENT '创建者ID',
  `owner_id`     VARCHAR(32)  NOT NULL COMMENT '群主ID（可转让，初始=创建者）',
  `group_avatar` VARCHAR(255) DEFAULT NULL COMMENT '群头像',
  `group_notice` VARCHAR(500) DEFAULT NULL COMMENT '群公告',
  `group_remark` VARCHAR(255) DEFAULT NULL COMMENT '群备注',
  `create_time`  BIGINT       NOT NULL COMMENT '创建时间戳（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_owner` (`owner_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '群组表';

-- ---------------------------------------------------------------------
-- 6. 群组成员表
-- ---------------------------------------------------------------------
CREATE TABLE `group_member_table` (
  `id`        VARCHAR(32) NOT NULL COMMENT '主键（UUID）',
  `group_id`  VARCHAR(32) NOT NULL COMMENT '群组ID',
  `user_id`   VARCHAR(32) NOT NULL COMMENT '用户ID',
  `join_time` BIGINT      NOT NULL COMMENT '加入时间戳（毫秒）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '群组成员表';

-- ---------------------------------------------------------------------
-- 7. 版本表（任务协同根节点）
-- ---------------------------------------------------------------------
CREATE TABLE `version_table` (
  `id`                VARCHAR(32)  NOT NULL COMMENT '版本ID',
  `name`              VARCHAR(100) NOT NULL COMMENT '版本名称（必填）',
  `description`       VARCHAR(1000) DEFAULT NULL COMMENT '描述',
  `plan_release_time` BIGINT       DEFAULT NULL COMMENT '计划发布时间戳（毫秒）',
  `status`            TINYINT      NOT NULL COMMENT '状态：0进行中/1已关闭/2已取消',
  `creator_id`        VARCHAR(32)  NOT NULL COMMENT '创建人ID（仅项目负责人可创建）',
  `create_time`       BIGINT       NOT NULL COMMENT '创建时间戳（毫秒）',
  `update_time`       BIGINT       DEFAULT NULL COMMENT '更新时间戳（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_creator` (`creator_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '版本表（任务协同根节点）';

-- ---------------------------------------------------------------------
-- 8. 需求单表
-- ---------------------------------------------------------------------
CREATE TABLE `requirement_table` (
  `id`          VARCHAR(32) NOT NULL COMMENT '需求单ID',
  `version_id`  VARCHAR(32) NOT NULL COMMENT '所属版本ID',
  `title`       VARCHAR(200) NOT NULL COMMENT '标题（必填）',
  `content`     TEXT        NOT NULL COMMENT '需求内容（必填）',
  `owner_id`    VARCHAR(32) NOT NULL COMMENT '负责人ID（当前，转包时变更）',
  `creator_id`  VARCHAR(32) NOT NULL COMMENT '创建人ID',
  `status`      TINYINT     NOT NULL COMMENT '状态：0开发中/1已完成/2已取消',
  `create_time` BIGINT      NOT NULL COMMENT '创建时间戳（毫秒）',
  `update_time` BIGINT      DEFAULT NULL COMMENT '更新时间戳（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_version` (`version_id`),
  KEY `idx_owner` (`owner_id`),
  KEY `idx_creator` (`creator_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '需求单表';

-- ---------------------------------------------------------------------
-- 9. bug单表
-- ---------------------------------------------------------------------
CREATE TABLE `bug_table` (
  `id`                VARCHAR(32)  NOT NULL COMMENT 'bug单ID',
  `version_id`        VARCHAR(32)  NOT NULL COMMENT '所属版本ID',
  `title`             VARCHAR(200) NOT NULL COMMENT '标题（必填）',
  `detail`            TEXT         NOT NULL COMMENT 'bug操作明细（必填）',
  `screenshot`        VARCHAR(1000) DEFAULT NULL COMMENT 'bug界面截图（文件地址，可JSON数组）',
  `owner_id`          VARCHAR(32)  NOT NULL COMMENT '负责人ID（当前，转包时变更）',
  `creator_id`        VARCHAR(32)  NOT NULL COMMENT '创建人ID（始终不变）',
  `reason`            TEXT         DEFAULT NULL COMMENT 'bug原因（当前负责人填写）',
  `solution`          TEXT         DEFAULT NULL COMMENT '解决方案（当前负责人填写）',
  `result_screenshot` VARCHAR(1000) DEFAULT NULL COMMENT '结果截图（文件地址）',
  `status`            TINYINT      NOT NULL COMMENT '状态：0开发中/1已完成/2已取消',
  `create_time`       BIGINT       NOT NULL COMMENT '创建时间戳（毫秒）',
  `update_time`       BIGINT       DEFAULT NULL COMMENT '更新时间戳（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_version` (`version_id`),
  KEY `idx_owner` (`owner_id`),
  KEY `idx_creator` (`creator_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'bug单表';

-- ---------------------------------------------------------------------
-- 10. 单据流转记录表（单实体 + 流转记录，链条：创建 -> 转包1 -> 转包2 ...）
-- ---------------------------------------------------------------------
CREATE TABLE `flow_record_table` (
  `id`           VARCHAR(32)  NOT NULL COMMENT '流转记录ID',
  `ticket_type`  TINYINT      NOT NULL COMMENT '单据类型：0需求单/1bug单',
  `ticket_id`    VARCHAR(32)  NOT NULL COMMENT '单据ID',
  `action`       TINYINT      NOT NULL COMMENT '动作：0创建/1转包',
  `operator_id`  VARCHAR(32)  NOT NULL COMMENT '操作人ID',
  `from_user_id` VARCHAR(32)  DEFAULT NULL COMMENT '转出人ID（转包时）',
  `to_user_id`   VARCHAR(32)  DEFAULT NULL COMMENT '转入人ID（转包时；创建时为负责人）',
  `remark`       VARCHAR(500) DEFAULT NULL COMMENT '备注（转包必填）',
  `create_time`  BIGINT       NOT NULL COMMENT '操作时间戳（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_ticket` (`ticket_type`, `ticket_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '单据流转记录表';
