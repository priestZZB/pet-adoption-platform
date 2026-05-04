package com.pet.gateway.controller;

import com.pet.common.result.Result;
import com.pet.common.util.FileUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    /**
     * 通用文件上传（单文件）
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(HttpServletRequest request,
                                              @RequestParam("file") MultipartFile file,
                                              @RequestParam(defaultValue = "common") String module) {
        String url = FileUtils.upload(file, module);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("上传成功", data);
    }

    /**
     * 多文件上传
     */
    @PostMapping("/upload/multi")
    public Result<Map<String, List<String>>> uploadMulti(HttpServletRequest request,
                                                         @RequestParam("files") List<MultipartFile> files,
                                                         @RequestParam(defaultValue = "common") String module) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = FileUtils.upload(file, module);
            urls.add(url);
        }
        Map<String, List<String>> data = new HashMap<>();
        data.put("urls", urls);
        return Result.success("上传成功", data);
    }
}