-- ============================================================
-- 宠物领养救助管理平台 — v6.0 迁移
-- 收货地址新增具体位置和门牌号字段
-- ============================================================

USE `pet_adoption`;

-- 新增具体位置字段
ALTER TABLE `mall_shipping_address`
    ADD COLUMN `specific_place` VARCHAR(255) DEFAULT NULL COMMENT '具体位置（小区/酒店/地标名称）'
    AFTER `district`;

-- 新增门牌号字段（选填）
ALTER TABLE `mall_shipping_address`
    ADD COLUMN `room_no` VARCHAR(100) DEFAULT NULL COMMENT '门牌号（选填）'
    AFTER `specific_place`;

-- 兼容旧数据
UPDATE `mall_shipping_address`
SET `specific_place` = `detail_address`
WHERE `specific_place` IS NULL AND `detail_address` IS NOT NULL;
