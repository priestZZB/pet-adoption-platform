package com.pet.framework.config;

import com.pet.common.util.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 文件上传路径配置
 * 从 application.yml 读取 pet.upload.path 注入 FileUtils
 */
@Configuration
public class UploadConfig {

    @Value("${pet.upload.path}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        FileUtils.setUploadDir(uploadPath);
    }
}
