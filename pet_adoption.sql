-- ============================================================
-- 宠物领养救助管理平台 — 数据库初始化脚本
-- 版本: v5.1 | 生成日期: 2026-05-25
-- 说明：从实体类同步生成，包含全部25+张表和初始数据
-- ============================================================

CREATE DATABASE IF NOT EXISTS `pet_adoption`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `pet_adoption`;

SET NAMES utf8mb4;

-- ============================================================
-- 删除旧表（按依赖顺序，子表先删）
-- ============================================================

DROP TABLE IF EXISTS `pet_comment_dislike`;
DROP TABLE IF EXISTS `pet_comment_like`;
DROP TABLE IF EXISTS `pet_comment`;
DROP TABLE IF EXISTS `pet_favorite`;
DROP TABLE IF EXISTS `pet_favorite_folder`;
DROP TABLE IF EXISTS `pet_review_record`;
DROP TABLE IF EXISTS `pet_image`;
DROP TABLE IF EXISTS `adopt_application`;
DROP TABLE IF EXISTS `adopt_exam_record`;
DROP TABLE IF EXISTS `adopt_question`;
DROP TABLE IF EXISTS `product_review`;
DROP TABLE IF EXISTS `mall_cart`;
DROP TABLE IF EXISTS `mall_order_item`;
DROP TABLE IF EXISTS `mall_order`;
DROP TABLE IF EXISTS `mall_shipping_address`;
DROP TABLE IF EXISTS `mall_product`;
DROP TABLE IF EXISTS `mall_category`;
DROP TABLE IF EXISTS `volunteer_visit_record`;
DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `ai_conversation`;
DROP TABLE IF EXISTS `pet_info`;
DROP TABLE IF EXISTS `pet_category`;
DROP TABLE IF EXISTS `user_notice_read`;
DROP TABLE IF EXISTS `sys_notification`;
DROP TABLE IF EXISTS `sys_feedback`;
DROP TABLE IF EXISTS `sys_notice`;
DROP TABLE IF EXISTS `sys_operation_log`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `banner`;
DROP TABLE IF EXISTS `sys_user`;

-- ============================================================
-- 一、系统管理表
-- ============================================================

-- 1.1 用户表
CREATE TABLE `sys_user` (
  `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`         varchar(50)  NOT NULL COMMENT '用户名（7位数字，自动生成）',
  `password`         varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname`         varchar(50)  DEFAULT NULL COMMENT '昵称',
  `phone`            varchar(20)  DEFAULT NULL COMMENT '手机号',
  `email`            varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar`           varchar(500) DEFAULT NULL COMMENT '头像URL',
  `real_name`        varchar(50)  DEFAULT NULL COMMENT '真实姓名',
  `id_card`          varchar(18)  DEFAULT NULL COMMENT '身份证号',
  `is_real_name`     tinyint      DEFAULT '0' COMMENT '是否实名认证（0否 1是）',
  `status`           tinyint      DEFAULT '1' COMMENT '状态（0禁用 1启用）',
  `volunteer_status` varchar(20)  DEFAULT 'NONE' COMMENT '志愿者状态（NONE未申请/PENDING待审核/APPROVED已通过/REJECTED已驳回）',
  `donor_status`     varchar(20)  DEFAULT 'NONE' COMMENT '送养人状态（NONE未申请/PENDING待审核/APPROVED已通过/REJECTED已驳回）',
  `is_super_admin`   tinyint      DEFAULT '0' COMMENT '是否超级管理员（0否 1是）',
  `created_at`       datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.2 角色表
CREATE TABLE `sys_role` (
  `id`         bigint      NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code`  varchar(20) NOT NULL COMMENT '角色编码（USER/USER_ADOPTER/VOLUNTEER/ADMIN）',
  `role_name`  varchar(50) NOT NULL COMMENT '角色名称',
  `created_at` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 1.3 用户角色关联表
CREATE TABLE `sys_user_role` (
  `id`      bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_sur_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sur_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 1.4 操作日志表
CREATE TABLE `sys_operation_log` (
  `id`         bigint      NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id`    bigint      DEFAULT NULL COMMENT '操作人ID',
  `username`   varchar(50) DEFAULT NULL COMMENT '操作人用户名',
  `module`     varchar(50) NOT NULL COMMENT '操作模块',
  `action`     varchar(200) NOT NULL COMMENT '操作内容描述',
  `ip`         varchar(50) DEFAULT NULL COMMENT '请求IP',
  `created_at` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 1.5 公告已读记录表
CREATE TABLE `user_notice_read` (
  `id`        bigint   NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id`   bigint   NOT NULL COMMENT '用户ID',
  `notice_id` bigint   NOT NULL COMMENT '公告ID',
  `read_at`   datetime DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_notice` (`user_id`, `notice_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_notice_id` (`notice_id`),
  CONSTRAINT `fk_unr_user`   FOREIGN KEY (`user_id`)   REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_unr_notice` FOREIGN KEY (`notice_id`) REFERENCES `sys_notice` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告已读记录表';

-- ============================================================
-- 二、公告、反馈与通知表
-- ============================================================

-- 2.1 公告表
CREATE TABLE `sys_notice` (
  `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title`      varchar(200) NOT NULL COMMENT '公告标题',
  `content`    text         NOT NULL COMMENT '公告内容',
  `status`     tinyint      DEFAULT '1' COMMENT '状态（0隐藏 1显示）',
  `created_at` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `updated_at` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 2.2 意见反馈表
CREATE TABLE `sys_feedback` (
  `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id`    bigint       NOT NULL COMMENT '用户ID',
  `content`    text         NOT NULL COMMENT '反馈内容',
  `images`     varchar(2000) DEFAULT NULL COMMENT '图片URL（多张逗号分隔）',
  `reply`      text         DEFAULT NULL COMMENT '管理员回复',
  `status`     tinyint      DEFAULT '0' COMMENT '状态（0未回复 1已回复）',
  `created_at` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='意见反馈表';

-- 2.3 站内通知表
CREATE TABLE `sys_notification` (
  `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id`    bigint       NOT NULL COMMENT '接收用户ID',
  `type`       varchar(50)  NOT NULL COMMENT '通知类型',
  `title`      varchar(200) NOT NULL COMMENT '通知标题',
  `content`    text         DEFAULT NULL COMMENT '通知内容',
  `related_id` bigint       DEFAULT NULL COMMENT '关联业务ID',
  `is_read`    tinyint      DEFAULT '0' COMMENT '是否已读（0未读 1已读）',
  `created_at` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知表';

-- 2.4 轮播图表
CREATE TABLE `banner` (
  `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `image_url`  varchar(500) NOT NULL COMMENT '图片URL',
  `title`      varchar(100) DEFAULT NULL COMMENT '标题',
  `sort_order` int          DEFAULT '0' COMMENT '排序号',
  `created_at` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- ============================================================
-- 三、宠物管理表
-- ============================================================

-- 3.1 宠物分类表
CREATE TABLE `pet_category` (
  `id`         bigint      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name`       varchar(50) NOT NULL COMMENT '分类名称',
  `sort_order` int         DEFAULT '0' COMMENT '排序号',
  `created_at` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物分类表';

-- 3.2 宠物信息表
CREATE TABLE `pet_info` (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '宠物ID',
  `user_id`       bigint       NOT NULL COMMENT '送养人ID',
  `category_id`   bigint       NOT NULL COMMENT '宠物分类ID',
  `name`          varchar(50)  NOT NULL COMMENT '宠物名称',
  `age`           varchar(20)  NOT NULL COMMENT '年龄',
  `gender`        varchar(10)  NOT NULL COMMENT '性别（male/female）',
  `is_neutered`   tinyint      DEFAULT '0' COMMENT '是否绝育（0否 1是）',
  `is_vaccinated` tinyint      DEFAULT '0' COMMENT '是否已接种疫苗（0否 1是）',
  `health_cert`   varchar(500) NOT NULL COMMENT '健康证明/体检报告图片URL',
  `personality`   text         NOT NULL COMMENT '性格描述',
  `habit`         text         DEFAULT NULL COMMENT '生活习惯',
  `reason`        text         NOT NULL COMMENT '送养原因',
  `status`        varchar(20)  DEFAULT 'PENDING' COMMENT '状态（PENDING待审/FIRST_PASS初审通过/APPROVED通过/REJECTED打回/ADOPTED已领养/OFFLINE已下架）',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '审核备注（打回原因）',
  `created_at`    datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `updated_at`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_pet_user`     FOREIGN KEY (`user_id`)     REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pet_category` FOREIGN KEY (`category_id`) REFERENCES `pet_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物信息表';

-- 3.3 宠物图片表
CREATE TABLE `pet_image` (
  `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `pet_id`     bigint       NOT NULL COMMENT '宠物ID',
  `image_url`  varchar(500) NOT NULL COMMENT '图片URL',
  `sort_order` int          DEFAULT '0' COMMENT '排序号（第1张作为封面）',
  `created_at` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  CONSTRAINT `fk_image_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物图片表';

-- 3.4 宠物收藏表
CREATE TABLE `pet_favorite` (
  `id`         bigint   NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id`    bigint   NOT NULL COMMENT '用户ID',
  `pet_id`     bigint   NOT NULL COMMENT '宠物ID',
  `folder_id`  bigint   DEFAULT NULL COMMENT '收藏夹ID（null为未分组）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pet` (`user_id`, `pet_id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_folder_id` (`folder_id`),
  CONSTRAINT `fk_fav_user`   FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fav_pet`    FOREIGN KEY (`pet_id`)  REFERENCES `pet_info` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fav_folder` FOREIGN KEY (`folder_id`) REFERENCES `pet_favorite_folder` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物收藏表';

-- 3.5 收藏夹表
CREATE TABLE `pet_favorite_folder` (
  `id`         bigint      NOT NULL AUTO_INCREMENT COMMENT '收藏夹ID',
  `user_id`    bigint      NOT NULL COMMENT '用户ID',
  `name`       varchar(50) NOT NULL COMMENT '收藏夹名称',
  `sort_order` int         DEFAULT '0' COMMENT '排序号',
  `created_at` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_folder_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏夹表';

-- 3.6 宠物审核记录表
CREATE TABLE `pet_review_record` (
  `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
  `pet_id`      bigint      NOT NULL COMMENT '宠物ID',
  `reviewer_id` bigint      NOT NULL COMMENT '审核人ID',
  `review_type` varchar(20) NOT NULL COMMENT '审核类型（FIRST初审/FINAL终审）',
  `action`      varchar(20) NOT NULL COMMENT '操作（APPROVED通过/REJECTED打回）',
  `remark`      varchar(500) DEFAULT NULL COMMENT '审核备注',
  `created_at`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_reviewer_id` (`reviewer_id`),
  CONSTRAINT `fk_review_pet`       FOREIGN KEY (`pet_id`)       REFERENCES `pet_info` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_reviewer`  FOREIGN KEY (`reviewer_id`)  REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物审核记录表';

-- 3.7 宠物评论表
CREATE TABLE `pet_comment` (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `pet_id`        bigint       NOT NULL COMMENT '宠物ID',
  `user_id`       bigint       NOT NULL COMMENT '评论人ID',
  `parent_id`     bigint       DEFAULT NULL COMMENT '回复的评论ID（null=根评论）',
  `reply_to`      bigint       DEFAULT NULL COMMENT '回复目标用户ID',
  `content`       text         NOT NULL COMMENT '评论内容',
  `images`        varchar(2000) DEFAULT NULL COMMENT '图片URL（逗号分隔）',
  `like_count`    int          DEFAULT '0' COMMENT '点赞数',
  `dislike_count` int          DEFAULT '0' COMMENT '心碎数（踩）',
  `status`        tinyint      DEFAULT '1' COMMENT '状态（0隐藏 1显示）',
  `created_at`    datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_comment_pet`  FOREIGN KEY (`pet_id`)  REFERENCES `pet_info` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物评论表';

-- 3.8 评论点赞表
CREATE TABLE `pet_comment_like` (
  `id`         bigint   NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `comment_id` bigint   NOT NULL COMMENT '评论ID',
  `user_id`    bigint   NOT NULL COMMENT '点赞用户ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
  KEY `idx_comment_id` (`comment_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `pet_comment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_like_user`    FOREIGN KEY (`user_id`)   REFERENCES `sys_user`   (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- 3.9 评论心碎表（踩）
CREATE TABLE `pet_comment_dislike` (
  `id`         bigint   NOT NULL AUTO_INCREMENT COMMENT '心碎ID',
  `comment_id` bigint   NOT NULL COMMENT '评论ID',
  `user_id`    bigint   NOT NULL COMMENT '用户ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dislike_comment_user` (`comment_id`, `user_id`),
  KEY `idx_comment_id` (`comment_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_dislike_comment` FOREIGN KEY (`comment_id`) REFERENCES `pet_comment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_dislike_user`    FOREIGN KEY (`user_id`)   REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论心碎表（踩）';

-- ============================================================
-- 四、志愿者管理表
-- ============================================================

-- 4.1 走访记录表
CREATE TABLE `volunteer_visit_record` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `volunteer_id` bigint       NOT NULL COMMENT '志愿者ID',
  `pet_id`       bigint       DEFAULT NULL COMMENT '关联宠物（可空）',
  `visit_date`   date         NOT NULL COMMENT '走访日期',
  `content`      text         NOT NULL COMMENT '走访内容',
  `images`       varchar(2000) DEFAULT NULL COMMENT '图片（逗号分隔URL）',
  `created_at`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer_id` (`volunteer_id`),
  KEY `idx_pet_id` (`pet_id`),
  CONSTRAINT `fk_visit_volunteer` FOREIGN KEY (`volunteer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_visit_pet`      FOREIGN KEY (`pet_id`)       REFERENCES `pet_info`  (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='走访记录表';

-- ============================================================
-- 五、领养管理表
-- ============================================================

-- 5.1 领养试题表
CREATE TABLE `adopt_question` (
  `id`             bigint      NOT NULL AUTO_INCREMENT COMMENT '试题ID',
  `question`       text        NOT NULL COMMENT '题目内容',
  `option_a`       varchar(255) NOT NULL COMMENT '选项A',
  `option_b`       varchar(255) NOT NULL COMMENT '选项B',
  `option_c`       varchar(255) NOT NULL COMMENT '选项C',
  `option_d`       varchar(255) DEFAULT NULL COMMENT '选项D（可空）',
  `correct_answer` char(1)     NOT NULL COMMENT '正确答案（A/B/C/D）',
  `created_at`     datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领养试题表';

-- 5.2 答题记录表
CREATE TABLE `adopt_exam_record` (
  `id`              bigint   NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id`         bigint   NOT NULL COMMENT '用户ID',
  `score`           int      NOT NULL COMMENT '得分',
  `total_questions` int      NOT NULL COMMENT '总题数',
  `is_passed`       tinyint  DEFAULT '0' COMMENT '是否通过（0否 1是）',
  `created_at`      datetime DEFAULT CURRENT_TIMESTAMP COMMENT '答题时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_exam_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题记录表';

-- 5.3 领养申请表
CREATE TABLE `adopt_application` (
  `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `user_id`     bigint      NOT NULL COMMENT '申请人ID',
  `pet_id`      bigint      NOT NULL COMMENT '申请领养的宠物ID',
  `living_env`  text        NOT NULL COMMENT '居住环境描述',
  `pet_exp`     text        DEFAULT NULL COMMENT '养宠经验',
  `commitment`  text        NOT NULL COMMENT '领养承诺',
  `status`      varchar(20) DEFAULT 'PENDING' COMMENT '状态（PENDING待审/APPROVED通过/REJECTED拒绝）',
  `created_at`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_at`  datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_apply_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_apply_pet`  FOREIGN KEY (`pet_id`)  REFERENCES `pet_info`  (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领养申请表';

-- ============================================================
-- 六、商城模块表
-- ============================================================

-- 6.1 商品分类表
CREATE TABLE `mall_category` (
  `id`         bigint      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name`       varchar(50) NOT NULL COMMENT '分类名称',
  `sort_order` int         DEFAULT '0' COMMENT '排序号',
  `created_at` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 6.2 商品表
CREATE TABLE `mall_product` (
  `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `category_id` bigint        NOT NULL COMMENT '分类ID',
  `name`        varchar(100)  NOT NULL COMMENT '商品名称',
  `description` text          DEFAULT NULL COMMENT '商品描述',
  `price`       decimal(10,2) NOT NULL COMMENT '价格',
  `stock`       int           DEFAULT '0' COMMENT '库存',
  `image`       varchar(500)  DEFAULT NULL COMMENT '商品主图URL',
  `status`      tinyint       DEFAULT '1' COMMENT '状态（0下架 1上架）',
  `created_at`  datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `mall_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 6.3 购物车表
CREATE TABLE `mall_cart` (
  `id`         bigint        NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id`    bigint        NOT NULL COMMENT '用户ID',
  `product_id` bigint        NOT NULL COMMENT '商品ID',
  `quantity`   int           NOT NULL DEFAULT '1' COMMENT '数量',
  `price`      decimal(10,2) NOT NULL COMMENT '加入时的价格快照',
  `created_at` datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_cart_user`    FOREIGN KEY (`user_id`)    REFERENCES `sys_user`   (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `mall_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 6.4 订单表
CREATE TABLE `mall_order` (
  `id`                bigint        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no`          varchar(50)   NOT NULL COMMENT '订单号',
  `user_id`           bigint        NOT NULL COMMENT '用户ID',
  `total_amount`      decimal(10,2) NOT NULL COMMENT '订单总金额',
  `status`            varchar(20)   DEFAULT 'PENDING_PAY' COMMENT '状态（PENDING_PAY待支付/PAID已支付/SHIPPED已发货/RECEIVED已收货/CANCELLED已取消）',
  `logistics_no`      varchar(100)  DEFAULT NULL COMMENT '物流单号',
  `logistics_status`  varchar(50)   DEFAULT NULL COMMENT '物流状态',
  `logistics_timeline` varchar(500) DEFAULT NULL COMMENT '物流时间线JSON',
  `courier_company`   varchar(20)   DEFAULT NULL COMMENT '快递公司',
  `receiver_name`     varchar(50)   NOT NULL COMMENT '收货人姓名',
  `receiver_phone`    varchar(20)   NOT NULL COMMENT '收货人电话',
  `receiver_address`  varchar(255)  NOT NULL COMMENT '收货地址',
  `created_at`        datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `updated_at`        datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 6.5 订单明细表
CREATE TABLE `mall_order_item` (
  `id`            bigint        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id`      bigint        NOT NULL COMMENT '订单ID',
  `product_id`    bigint        NOT NULL COMMENT '商品ID',
  `product_name`  varchar(100)  NOT NULL COMMENT '商品名称（下单时快照）',
  `product_image` varchar(500)  DEFAULT NULL COMMENT '商品图片（下单时快照）',
  `quantity`      int           NOT NULL COMMENT '购买数量',
  `price`         decimal(10,2) NOT NULL COMMENT '购买单价',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_item_order`   FOREIGN KEY (`order_id`)   REFERENCES `mall_order`   (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_product` FOREIGN KEY (`product_id`) REFERENCES `mall_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- 6.6 收货地址表
CREATE TABLE `mall_shipping_address` (
  `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id`          bigint       NOT NULL COMMENT '用户ID',
  `receiver_name`    varchar(50)  NOT NULL COMMENT '收货人姓名',
  `receiver_phone`   varchar(20)  NOT NULL COMMENT '收货人电话',
  `province`         varchar(50)  DEFAULT NULL COMMENT '省份',
  `city`             varchar(50)  DEFAULT NULL COMMENT '城市',
  `district`         varchar(50)  DEFAULT NULL COMMENT '区县',
  `detail_address`   varchar(255) DEFAULT NULL COMMENT '详细地址',
  `receiver_address` varchar(500) DEFAULT NULL COMMENT '完整收货地址',
  `is_default`       tinyint      DEFAULT '0' COMMENT '是否默认（0否 1是）',
  `created_at`       datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- 6.7 商品评价表
CREATE TABLE `product_review` (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `product_id`    bigint       NOT NULL COMMENT '商品ID',
  `order_item_id` bigint       NOT NULL COMMENT '订单项ID（唯一约束，一单一项只能评一次）',
  `user_id`       bigint       NOT NULL COMMENT '评价人ID',
  `rating`        int          NOT NULL COMMENT '评分（1-5星）',
  `content`       text         DEFAULT NULL COMMENT '文字评价',
  `images`        varchar(2000) DEFAULT NULL COMMENT '晒图（逗号分隔）',
  `tags`          varchar(500) DEFAULT NULL COMMENT '评价标签（逗号分隔）',
  `parent_id`     bigint       DEFAULT NULL COMMENT '关联的原始评价ID（追加评价时）',
  `is_additional` tinyint      DEFAULT '0' COMMENT '是否追加评价（0否 1是）',
  `is_anonymous`  tinyint      DEFAULT '0' COMMENT '是否匿名（0否 1是）',
  `status`        tinyint      DEFAULT '1' COMMENT '状态（0隐藏 1显示）',
  `created_at`    datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_item` (`order_item_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_rating` (`rating`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_review_product`    FOREIGN KEY (`product_id`)    REFERENCES `mall_product`   (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_order_item` FOREIGN KEY (`order_item_id`) REFERENCES `mall_order_item` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_user`       FOREIGN KEY (`user_id`)       REFERENCES `sys_user`       (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

-- ============================================================
-- 七、AI咨询模块
-- ============================================================

-- 7.1 AI对话记录表
CREATE TABLE `ai_conversation` (
  `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id`    bigint       NOT NULL COMMENT '用户ID',
  `session_id` varchar(36)  NOT NULL DEFAULT '' COMMENT '会话ID',
  `question`   text         NOT NULL COMMENT '用户提问',
  `answer`     text         NOT NULL COMMENT 'AI回答',
  `is_deleted` tinyint      DEFAULT '0' COMMENT '软删除标记（0正常 1已删）',
  `created_at` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '提问时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session` (`session_id`),
  CONSTRAINT `fk_ai_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话记录表';

-- ============================================================
-- 八、聊天模块
-- ============================================================

-- 8.1 聊天消息表
CREATE TABLE `chat_message` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id`   bigint       NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint       NOT NULL COMMENT '接收者ID',
  `pet_id`      bigint       NOT NULL COMMENT '关联宠物ID',
  `content`     text         DEFAULT NULL COMMENT '文字内容',
  `image_url`   varchar(500) DEFAULT NULL COMMENT '图片URL',
  `is_read`     tinyint      DEFAULT '0' COMMENT '是否已读（0未读 1已读）',
  `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender` (`sender_id`),
  KEY `idx_receiver` (`receiver_id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_msg_sender`   FOREIGN KEY (`sender_id`)   REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_msg_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_msg_pet`      FOREIGN KEY (`pet_id`)      REFERENCES `pet_info`  (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- ============================================================
-- 九、初始数据
-- ============================================================

-- 9.1 插入4个角色
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`) VALUES
(1, 'USER',         '普通用户'),
(2, 'USER_ADOPTER', '送养人'),
(3, 'VOLUNTEER',    '志愿者'),
(4, 'ADMIN',        '管理员');

-- 9.2 插入管理员账号（密码: admin060110，已用BCrypt加密）
-- 如需修改密码，用 https://bcrypt-generator.com/ 生成新hash替换
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `phone`, `status`, `is_super_admin`) VALUES
(1, 'admin', '$2a$12$wO2gCiYyXKou8DinB4EjROkYsBnNAPp3CX64kpW.PAaTiMKFb3qLC', '管理员', '13078929463', 1, 1);

-- 9.3 分配管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 4);




