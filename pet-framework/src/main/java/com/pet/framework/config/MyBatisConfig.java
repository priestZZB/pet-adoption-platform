package com.pet.framework.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置 — 扫描所有业务模块的 mapper 接口
 */
@Configuration
@MapperScan("com.pet.**.mapper")
public class MyBatisConfig {
}