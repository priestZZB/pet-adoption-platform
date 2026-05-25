-- ============================================================
-- 瀹犵墿棰嗗吇鏁戝姪绠＄悊骞冲彴 鈥?鏁版嵁搴撳垵濮嬪寲鑴氭湰
-- 鐗堟湰: v5.1 | 鏇存柊: 2026-05-22
-- 鏂板: pet_comment/pet_comment_like 瀹犵墿璇勮+鐐硅禐, product_review 鍟嗗搧璇勪环
-- ============================================================

CREATE DATABASE IF NOT EXISTS `pet_adoption` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pet_adoption`;

SET NAMES utf8mb4;

-- ============================================================
-- 鍒犻櫎鏃ц〃锛堟寜渚濊禆椤哄簭锛屽瓙琛ㄥ厛鍒狅級
-- ============================================================

DROP TABLE IF EXISTS `user_notice_read`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_operation_log`;
DROP TABLE IF EXISTS `sys_notification`;
DROP TABLE IF EXISTS `sys_feedback`;
DROP TABLE IF EXISTS `volunteer_visit_record`;
DROP TABLE IF EXISTS pet_comment_dislike;
DROP TABLE IF EXISTS `pet_comment_like`;
DROP TABLE IF EXISTS `pet_comment`;
DROP TABLE IF EXISTS `pet_review_record`;
DROP TABLE IF EXISTS `pet_image`;
DROP TABLE IF EXISTS `pet_favorite`;
DROP TABLE IF EXISTS `adopt_application`;
DROP TABLE IF EXISTS `adopt_exam_record`;
DROP TABLE IF EXISTS `adopt_question`;
DROP TABLE IF EXISTS `mall_cart`;
DROP TABLE IF EXISTS `mall_shipping_address`;
DROP TABLE IF EXISTS `product_review`;
DROP TABLE IF EXISTS `mall_order_item`;
DROP TABLE IF EXISTS `mall_order`;
DROP TABLE IF EXISTS `mall_product`;
DROP TABLE IF EXISTS `mall_category`;
DROP TABLE IF EXISTS `pet_info`;
DROP TABLE IF EXISTS `pet_category`;
DROP TABLE IF EXISTS `ai_conversation`;
DROP TABLE IF EXISTS `banner`;
DROP TABLE IF EXISTS `sys_notice`;
DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;

-- ============================================================
-- 鐢ㄦ埛妯″潡
-- ============================================================

-- 鐢ㄦ埛琛?CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐢ㄦ埛鍚?,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瀵嗙爜锛圔Crypt鍔犲瘑锛?,
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鏄电О',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鎵嬫満鍙?,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '閭',
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '澶村儚URL',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鐪熷疄濮撳悕',
  `id_card` varchar(18) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '韬唤璇佸彿',
  `is_real_name` tinyint DEFAULT '0' COMMENT '鏄惁瀹炲悕璁よ瘉锛?鍚?1鏄級',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵€侊紙0绂佺敤 1鍚敤锛?,
  `volunteer_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'NONE' COMMENT '蹇楁効鑰呯姸鎬侊紙NONE鏈敵璇?PENDING寰呭鏍?APPROVED宸查€氳繃/REJECTED宸查┏鍥烇級',
  `donor_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'NONE' COMMENT '閫佸吇浜虹姸鎬侊紙NONE鏈敵璇?PENDING寰呭鏍?APPROVED宸查€氳繃/REJECTED宸查┏鍥烇級',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鐢ㄦ埛琛?;

-- 瑙掕壊琛?CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '瑙掕壊ID',
  `role_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瑙掕壊缂栫爜锛圲SER/USER_ADOPTER/VOLUNTEER/ADMIN锛?,
  `role_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瑙掕壊鍚嶇О',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瑙掕壊琛?;

-- 鐢ㄦ埛瑙掕壊鍏宠仈琛?CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `role_id` bigint NOT NULL COMMENT '瑙掕壊ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_sur_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sur_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鐢ㄦ埛瑙掕壊鍏宠仈琛?;

-- 鎿嶄綔鏃ュ織琛?CREATE TABLE `sys_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏃ュ織ID',
  `user_id` bigint DEFAULT NULL COMMENT '鎿嶄綔浜篒D',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鎿嶄綔浜虹敤鎴峰悕',
  `module` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鎿嶄綔妯″潡',
  `action` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鎿嶄綔鍐呭鎻忚堪',
  `ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '璇锋眰IP',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鎿嶄綔鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鎿嶄綔鏃ュ織琛?;

-- 鍏憡琛?CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍏憡ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍏憡鏍囬',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍏憡鍐呭',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵€侊紙0闅愯棌 1鏄剧ず锛?,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍙戝竷鏃堕棿',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鍏憡琛?;

-- 鍏憡宸茶璁板綍琛?CREATE TABLE `user_notice_read` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `notice_id` bigint NOT NULL COMMENT '鍏憡ID',
  `read_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_notice` (`user_id`,`notice_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `fnr_notice` (`notice_id`),
  CONSTRAINT `fnr_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fnr_notice` FOREIGN KEY (`notice_id`) REFERENCES `sys_notice` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鍏憡宸茶璁板綍琛?;

-- 鎰忚鍙嶉琛?CREATE TABLE `sys_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍙嶉ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍙嶉鍐呭',
  `images` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍥剧墖URL锛堝寮犻€楀彿鍒嗛殧锛?,
  `reply` text COLLATE utf8mb4_unicode_ci COMMENT '绠＄悊鍛樺洖澶?,
  `status` tinyint DEFAULT '0' COMMENT '鐘舵€侊紙0鏈洖澶?1宸插洖澶嶏級',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鎻愪氦鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鎰忚鍙嶉琛?;

-- 绔欏唴閫氱煡琛?CREATE TABLE `sys_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '閫氱煡ID',
  `user_id` bigint NOT NULL COMMENT '鎺ユ敹鐢ㄦ埛ID',
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '閫氱煡绫诲瀷',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '閫氱煡鏍囬',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '閫氱煡鍐呭',
  `related_id` bigint DEFAULT NULL COMMENT '鍏宠仈涓氬姟ID',
  `is_read` tinyint DEFAULT '0' COMMENT '鏄惁宸茶锛?鏈 1宸茶锛?,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_read` (`user_id`,`is_read`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绔欏唴閫氱煡琛?;

-- 杞挱鍥捐〃
CREATE TABLE `banner` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_url` varchar(500) NOT NULL COMMENT '鍥剧墖URL',
  `title` varchar(100) DEFAULT NULL COMMENT '鏍囬',
  `sort_order` int DEFAULT '0' COMMENT '鎺掑簭鍙?,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='杞挱鍥捐〃';

-- ============================================================
-- 瀹犵墿妯″潡
-- ============================================================

-- 瀹犵墿鍒嗙被琛?CREATE TABLE `pet_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍒嗙被ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍒嗙被鍚嶇О',
  `sort_order` int DEFAULT '0' COMMENT '鎺掑簭鍙?,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瀹犵墿鍒嗙被琛?;

-- 瀹犵墿淇℃伅琛?CREATE TABLE `pet_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '瀹犵墿ID',
  `user_id` bigint NOT NULL COMMENT '閫佸吇浜篒D',
  `category_id` bigint NOT NULL COMMENT '瀹犵墿鍒嗙被ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瀹犵墿鍚嶇О',
  `age` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '骞撮緞',
  `gender` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鎬у埆锛坢ale/female锛?,
  `is_neutered` tinyint DEFAULT '0' COMMENT '鏄惁缁濊偛锛?鍚?1鏄級',
  `is_vaccinated` tinyint DEFAULT '0' COMMENT '鏄惁宸叉帴绉嶇柅鑻楋紙0鍚?1鏄級',
  `health_cert` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍋ュ悍璇佹槑/浣撴鎶ュ憡鍥剧墖URL',
  `personality` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鎬ф牸鎻忚堪',
  `habit` text COLLATE utf8mb4_unicode_ci COMMENT '鐢熸椿涔犳儻',
  `reason` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '閫佸吇鍘熷洜',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '鐘舵€侊紙PENDING寰呭/APPROVED閫氳繃/REJECTED鎵撳洖/ADOPTED宸查鍏?OFFLINE宸蹭笅鏋讹級',
  `review_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '瀹℃牳澶囨敞锛堟墦鍥炲師鍥狅級',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍙戝竷鏃堕棿',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_pet_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pet_category` FOREIGN KEY (`category_id`) REFERENCES `pet_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瀹犵墿淇℃伅琛?;

-- 瀹犵墿鍥剧墖琛?CREATE TABLE `pet_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍥剧墖ID',
  `pet_id` bigint NOT NULL COMMENT '瀹犵墿ID',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍥剧墖URL',
  `sort_order` int DEFAULT '0' COMMENT '鎺掑簭鍙凤紙绗?寮犱綔涓哄皝闈級',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '涓婁紶鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  CONSTRAINT `fk_image_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瀹犵墿鍥剧墖琛?;

-- 瀹犵墿鏀惰棌琛?CREATE TABLE `pet_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏀惰棌ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `pet_id` bigint NOT NULL COMMENT '瀹犵墿ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鏀惰棌鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pet` (`user_id`,`pet_id`),
  KEY `idx_pet_id` (`pet_id`),
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fav_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瀹犵墿鏀惰棌琛?;

-- 瀹犵墿瀹℃牳璁板綍琛?CREATE TABLE `pet_review_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '瀹℃牳璁板綍ID',
  `pet_id` bigint NOT NULL COMMENT '瀹犵墿ID',
  `reviewer_id` bigint NOT NULL COMMENT '瀹℃牳浜篒D',
  `review_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瀹℃牳绫诲瀷锛團IRST鍒濆/FINAL缁堝锛?,
  `action` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鎿嶄綔锛圓PPROVED閫氳繃/REJECTED鎵撳洖锛?,
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '瀹℃牳澶囨敞',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '瀹℃牳鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_reviewer_id` (`reviewer_id`),
  CONSTRAINT `fk_review_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet_info` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瀹犵墿瀹℃牳璁板綍琛?;

-- 瀹犵墿璇勮琛?CREATE TABLE `pet_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璇勮ID',
  `pet_id` bigint NOT NULL COMMENT '瀹犵墿ID',
  `user_id` bigint NOT NULL COMMENT '璇勮浜篒D',
  `parent_id` bigint DEFAULT NULL COMMENT '鍥炲鐨勮瘎璁篒D锛坣ull=鏍硅瘎璁猴級',
  `reply_to` bigint DEFAULT NULL COMMENT '鍥炲鐩爣鐢ㄦ埛ID',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '璇勮鍐呭',
  `images` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍥剧墖URL锛堥€楀彿鍒嗛殧锛屾渶澶?寮狅級',
  `like_count` int DEFAULT '0' COMMENT '鐐硅禐鏁?,
  `status` tinyint DEFAULT '1' COMMENT '鐘舵€侊紙0閫佸吇浜洪殣钘?1鏄剧ず锛?,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '璇勮鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_comment_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet_info` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瀹犵墿璇勮琛?;

-- 璇勮鐐硅禐琛?CREATE TABLE `pet_comment_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐐硅禐ID',
  `comment_id` bigint NOT NULL COMMENT '璇勮ID',
  `user_id` bigint NOT NULL COMMENT '鐐硅禐鐢ㄦ埛ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鐐硅禐鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`,`user_id`),
  KEY `idx_comment_id` (`comment_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `pet_comment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='璇勮鐐硅禐琛?;

-- 评论心碎表（踩）
CREATE TABLE pet_comment_dislike (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '心碎ID',
  comment_id bigint NOT NULL COMMENT '评论ID',
  user_id bigint NOT NULL COMMENT '用户ID',
  created_at datetime DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dislike_comment_user (comment_id,user_id),
  KEY idx_comment_id (comment_id),
  KEY idx_user_id (user_id),
  CONSTRAINT k_dislike_comment FOREIGN KEY (comment_id) REFERENCES pet_comment (id) ON DELETE CASCADE,
  CONSTRAINT k_dislike_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论心碎表（踩）';

-- 蹇楁効鑰呰蛋璁胯褰曡〃
CREATE TABLE `volunteer_visit_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璁板綍ID',
  `volunteer_id` bigint NOT NULL COMMENT '蹇楁効鑰匢D',
  `pet_id` bigint DEFAULT NULL COMMENT '鍏宠仈瀹犵墿锛堝彲绌猴級',
  `visit_date` date NOT NULL COMMENT '璧拌鏃ユ湡',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '璧拌鍐呭',
  `images` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍥剧墖锛堥€楀彿鍒嗛殧URL锛?,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鎻愪氦鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer_id` (`volunteer_id`),
  KEY `idx_pet_id` (`pet_id`),
  CONSTRAINT `fk_visit_volunteer` FOREIGN KEY (`volunteer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_visit_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet_info` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='璧拌璁板綍琛?;

-- ============================================================
-- 棰嗗吇妯″潡
-- ============================================================

-- 棰嗗吇璇曢琛?CREATE TABLE `adopt_question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璇曢ID',
  `question` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '棰樼洰鍐呭',
  `option_a` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '閫夐」A',
  `option_b` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '閫夐」B',
  `option_c` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '閫夐」C',
  `option_d` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '閫夐」D',
  `correct_answer` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姝ｇ‘绛旀锛圓/B/C/D锛?,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='棰嗗吇璇曢琛?;

-- 绛旈璁板綍琛?CREATE TABLE `adopt_exam_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璁板綍ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `score` int NOT NULL COMMENT '寰楀垎',
  `total_questions` int NOT NULL COMMENT '鎬婚鏁?,
  `is_passed` tinyint DEFAULT '0' COMMENT '鏄惁閫氳繃锛?鍚?1鏄級',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绛旈鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_exam_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绛旈璁板綍琛?;

-- 棰嗗吇鐢宠琛?CREATE TABLE `adopt_application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐢宠ID',
  `user_id` bigint NOT NULL COMMENT '鐢宠浜篒D',
  `pet_id` bigint NOT NULL COMMENT '鐢宠棰嗗吇鐨勫疇鐗㊣D',
  `living_env` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '灞呬綇鐜鎻忚堪',
  `pet_exp` text COLLATE utf8mb4_unicode_ci COMMENT '鍏诲疇缁忛獙',
  `commitment` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '棰嗗吇鎵胯',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '鐘舵€侊紙PENDING寰呭/APPROVED閫氳繃/REJECTED鎷掔粷锛?,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鐢宠鏃堕棿',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_apply_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_apply_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='棰嗗吇鐢宠琛?;

-- ============================================================
-- 鍟嗗煄妯″潡
-- ============================================================

-- 鍟嗗搧鍒嗙被琛?CREATE TABLE `mall_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍒嗙被ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍒嗙被鍚嶇О',
  `sort_order` int DEFAULT '0' COMMENT '鎺掑簭鍙?,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鍟嗗搧鍒嗙被琛?;

-- 鍟嗗搧琛?CREATE TABLE `mall_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍟嗗搧ID',
  `category_id` bigint NOT NULL COMMENT '鍒嗙被ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍟嗗搧鍚嶇О',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '鍟嗗搧鎻忚堪',
  `price` decimal(10,2) NOT NULL COMMENT '浠锋牸',
  `stock` int DEFAULT '0' COMMENT '搴撳瓨',
  `image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍟嗗搧涓诲浘URL',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵€侊紙0涓嬫灦 1涓婃灦锛?,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `mall_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鍟嗗搧琛?;

-- 璁㈠崟琛?CREATE TABLE `mall_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璁㈠崟ID',
  `order_no` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '璁㈠崟鍙?,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '璁㈠崟鎬婚噾棰?,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING_PAY' COMMENT '鐘舵€侊紙PENDING_PAY寰呮敮浠?PAID宸叉敮浠?SHIPPED宸插彂璐?RECEIVED宸叉敹璐?CANCELLED宸插彇娑堬級',
  `logistics_no` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鐗╂祦鍗曞彿',
  `logistics_status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鐗╂祦鐘舵€?,
  `receiver_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏀惰揣浜哄鍚?,
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏀惰揣浜虹數璇?,
  `receiver_address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏀惰揣鍦板潃',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '涓嬪崟鏃堕棿',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `logistics_timeline` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鐗╂祦鏃堕棿绾縅SON',
  `courier_company` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '蹇€掑叕鍙?,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='璁㈠崟琛?;

-- 鏀惰揣鍦板潃琛?CREATE TABLE `mall_shipping_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍦板潃ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `receiver_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏀惰揣浜哄鍚?,
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏀惰揣浜虹數璇?,
  `province` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鐪佷唤',
  `city` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍩庡競',
  `district` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍖哄幙',
  `detail_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '璇︾粏鍦板潃',
  `receiver_address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '瀹屾暣鏀惰揣鍦板潃',
  `is_default` tinyint DEFAULT '0' COMMENT '鏄惁榛樿锛?鍚?1鏄級',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鏀惰揣鍦板潃琛?;

-- 璐墿杞﹁〃
CREATE TABLE `mall_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璐墿杞D',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `product_id` bigint NOT NULL COMMENT '鍟嗗搧ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '鏁伴噺',
  `price` decimal(10,2) NOT NULL COMMENT '鍔犲叆鏃剁殑浠锋牸蹇収',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `mall_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='璐墿杞﹁〃';

-- 璁㈠崟鏄庣粏琛?CREATE TABLE `mall_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏄庣粏ID',
  `order_id` bigint NOT NULL COMMENT '璁㈠崟ID',
  `product_id` bigint NOT NULL COMMENT '鍟嗗搧ID',
  `product_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍟嗗搧鍚嶇О锛堜笅鍗曟椂蹇収锛?,
  `product_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍟嗗搧鍥剧墖锛堜笅鍗曟椂蹇収锛?,
  `quantity` int NOT NULL COMMENT '璐拱鏁伴噺',
  `price` decimal(10,2) NOT NULL COMMENT '璐拱鍗曚环',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `mall_order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_product` FOREIGN KEY (`product_id`) REFERENCES `mall_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='璁㈠崟鏄庣粏琛?;

-- 鍟嗗搧璇勪环琛?CREATE TABLE `product_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璇勪环ID',
  `product_id` bigint NOT NULL COMMENT '鍟嗗搧ID',
  `order_item_id` bigint NOT NULL COMMENT '璁㈠崟椤笽D锛堝敮涓€绾︽潫锛屼竴鍗曚竴椤瑰彧鑳借瘎涓€娆★級',
  `user_id` bigint NOT NULL COMMENT '璇勪环浜篒D',
  `rating` int NOT NULL COMMENT '璇勫垎锛?-5鏄燂級',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '鏂囧瓧璇勪环',
  `images` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鏅掑浘锛堥€楀彿鍒嗛殧锛屾渶澶?寮狅級',
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '璇勪环鏍囩锛堥€楀彿鍒嗛殧锛?,
  `parent_id` bigint DEFAULT NULL COMMENT '鍏宠仈鐨勫師濮嬭瘎浠稩D锛堣拷鍔犺瘎浠锋椂锛?,
  `is_additional` tinyint DEFAULT '0' COMMENT '鏄惁杩藉姞璇勪环锛?鍚?1鏄級',
  `is_anonymous` tinyint DEFAULT '0' COMMENT '鏄惁鍖垮悕锛?鍚?1鏄級',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵€侊紙0闅愯棌 1鏄剧ず锛?,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '璇勪环鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_item` (`order_item_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_rating` (`rating`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_review_product` FOREIGN KEY (`product_id`) REFERENCES `mall_product` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_order_item` FOREIGN KEY (`order_item_id`) REFERENCES `mall_order_item` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鍟嗗搧璇勪环琛?;

-- ============================================================
-- AI鍜ㄨ妯″潡
-- ============================================================

-- AI瀵硅瘽璁板綍琛?CREATE TABLE `ai_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璁板綍ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `question` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐢ㄦ埛鎻愰棶',
  `answer` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'AI鍥炵瓟',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鎻愰棶鏃堕棿',
  `session_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '浼氳瘽ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '杞垹闄ゆ爣璁帮紙0姝ｅ父 1鍒犻櫎锛?,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session` (`session_id`),
  CONSTRAINT `fk_ai_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI瀵硅瘽璁板綍琛?;

-- ============================================================
-- 鑱婂ぉ妯″潡
-- ============================================================

-- 鑱婂ぉ娑堟伅琛?CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '娑堟伅ID',
  `sender_id` bigint NOT NULL COMMENT '鍙戦€佽€匢D',
  `receiver_id` bigint NOT NULL COMMENT '鎺ユ敹鑰匢D',
  `pet_id` bigint NOT NULL COMMENT '鍏宠仈瀹犵墿ID',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '鏂囧瓧鍐呭',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍥剧墖URL',
  `is_read` tinyint DEFAULT '0' COMMENT '鏄惁宸茶锛?鏈 1宸茶锛?,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍙戦€佹椂闂?,
  PRIMARY KEY (`id`),
  KEY `idx_sender` (`sender_id`),
  KEY `idx_receiver` (`receiver_id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_msg_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_msg_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鑱婂ぉ娑堟伅琛?;


-- ============================================================
-- 鍩虹鏁版嵁鍒濆鍖?-- ============================================================

-- 鎻掑叆4涓鑹诧紙瀵嗙爜 admin060110 鐨凚Crypt鍔犲瘑锛?INSERT INTO `sys_role` (`id`, `role_code`, `role_name`) VALUES
(1, 'USER', '鏅€氱敤鎴?),
(2, 'USER_ADOPTER', '閫佸吇浜?),
(3, 'VOLUNTEER', '蹇楁効鑰?),
(4, 'ADMIN', '绠＄悊鍛?);

-- 鎻掑叆绠＄悊鍛樿处鍙凤紙瀵嗙爜: admin060110锛屽凡鐢˙Crypt鍔犲瘑锛?INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `phone`, `status`) VALUES
(1, 'admin', '$2a$10$5DiDsrcsYn6SuC6ifk2Sj.vlYisrsFfna3.DMw1guvkiRbQvnHbea', '绠＄悊鍛?, '13078929463', 1);

-- 鍒嗛厤绠＄悊鍛樿鑹?INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 4);


-- ============================================================
-- 新增：收藏夹表
-- ============================================================
DROP TABLE IF EXISTS pet_favorite_folder;
CREATE TABLE pet_favorite_folder (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '收藏夹ID',
  user_id bigint NOT NULL COMMENT '用户ID',
  
ame varchar(50) NOT NULL COMMENT '收藏夹名称',
  sort_order int DEFAULT '0' COMMENT '排序号',
  created_at datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  CONSTRAINT k_folder_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏夹表';

