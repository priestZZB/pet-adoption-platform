package com.pet.framework.config;

import com.pet.common.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * 文件上传路径配置
 * 从 application.yml 读取 pet.upload.path 注入 FileUtils
 */
@Slf4j
@Configuration
public class UploadConfig {

    @Value("${pet.upload.path}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        FileUtils.setUploadDir(uploadPath);
        log.info("文件上传路径已配置: {}", uploadPath);

        // 确保上传根目录存在
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("上传根目录已自动创建: {}", uploadPath);
            } else {
                log.warn("上传根目录创建失败，请检查权限: {}", uploadPath);
            }
        }
        if (!dir.canWrite()) {
            log.error("上传根目录不可写，文件上传将失败: {}", uploadPath);
        }
    }
}
