-- ============================================================
-- 宠物领养救助管理平台 — MySQL 首次初始化
-- Docker 容器首次启动时自动执行（放入 docker-entrypoint-initdb.d）
-- ============================================================

CREATE DATABASE IF NOT EXISTS `pet_adoption` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pet_adoption`;

-- 下面把宠物网站开发目录下的 pet_adoption.sql 全部贴进来
-- 建议复制 C:\Users\Priest\Desktop\宠物网站开发\pet_adoption.sql 的完整内容
