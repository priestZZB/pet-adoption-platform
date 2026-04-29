package com.pet.common.util;

import com.pet.common.exception.BusinessException;
import com.pet.common.enums.ResultCodeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件工具类
 */
public class FileUtils {

    /** 上传根目录 */
    public static final String UPLOAD_DIR = "/data/pet/uploads";
    /** 最大文件大小 10MB */
    private static final long MAX_SIZE = 10 * 1024 * 1024L;
    /** 允许的图片扩展名 */
    private static final String[] ALLOWED_EXT = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"};

    /**
     * 上传文件
     * @param file   文件
     * @param module 模块目录，如 avatar / pet / health / feedback / visit
     * @return 相对路径，如 /pet/2026/04/27/uuid.jpg
     */
    public static String upload(MultipartFile file, String module) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.FILE_EMPTY);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ResultCodeEnum.FILE_SIZE_EXCEED);
        }

        String ext = getExtension(file.getOriginalFilename());
        if (!isAllowed(ext)) {
            throw new BusinessException(ResultCodeEnum.FILE_TYPE_ERROR);
        }

        String dir = UPLOAD_DIR + "/" + module + "/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        File dest = new File(dir, fileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(ResultCodeEnum.FILE_UPLOAD_ERROR, "文件上传失败");
        }

        return "/" + module + "/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/" + fileName;
    }

    /**
     * 获取文件大小（可读格式）
     */
    public static String getReadableSize(long bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return String.format("%.1fKB", bytes / 1024.0);
        return String.format("%.1fMB", bytes / (1024.0 * 1024.0));
    }

    private static String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    private static boolean isAllowed(String ext) {
        for (String e : ALLOWED_EXT) {
            if (e.equals(ext)) return true;
        }
        return false;
    }
}