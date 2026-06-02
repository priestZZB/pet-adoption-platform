package com.pet.module.adopt.controller;

import com.github.pagehelper.PageInfo;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.adopt.model.entity.AdoptQuestion;
import com.pet.module.adopt.model.vo.AdoptApplyVo;
import com.pet.module.adopt.service.AdoptService;
import com.pet.module.adopt.service.QuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log("领养管理")
@Api(tags = "管理员-领养管理")
@RestController
@RequestMapping("/api/admin/adopt")
@RequireRole("ADMIN")
public class AdminQuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AdoptService adoptService;

    /**
     * 试题列表（分页）
     */
    @ApiOperation("试题列表（分页）")
    @GetMapping("/questions")
    public Result<PageInfo<AdoptQuestion>> questions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<AdoptQuestion> list = questionService.getQuestionList(page, size);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 新增试题
     */
    @ApiOperation("新增试题")
    @PostMapping("/questions")
    public Result<String> add(@RequestParam String question,
                              @RequestParam String optionA,
                              @RequestParam String optionB,
                              @RequestParam String optionC,
                              @RequestParam(required = false) String optionD,
                              @RequestParam String correctAnswer) {
        questionService.add(question, optionA, optionB, optionC, optionD, correctAnswer);
        return Result.success("新增成功");
    }

    /**
     * 编辑试题
     */
    @ApiOperation("编辑试题")
    @PutMapping("/questions/{id}")
    public Result<String> update(@PathVariable Long id,
                                 @RequestParam String question,
                                 @RequestParam String optionA,
                                 @RequestParam String optionB,
                                 @RequestParam String optionC,
                                 @RequestParam(required = false) String optionD,
                                 @RequestParam String correctAnswer) {
        questionService.update(id, question, optionA, optionB, optionC, optionD, correctAnswer);
        return Result.success("修改成功");
    }

    /**
     * 删除试题
     */
    @ApiOperation("删除试题")
    @DeleteMapping("/questions/{id}")
    public Result<String> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除试题
     */
    @ApiOperation("批量删除试题")
    @PostMapping("/questions/batch-delete")
    public Result<String> batchDelete(@RequestBody Map<String, Object> body) {
        List<Long> ids = extractLongList(body, "ids");
        questionService.batchDelete(ids);
        return Result.success("批量删除成功");
    }

    /**
     * 批量导入试题（CSV 格式）
     */
    @ApiOperation("批量导入试题（CSV）")
    @PostMapping("/questions/import")
    public Result<String> importQuestions(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(ResultCodeEnum.PARAM_INVALID, "请上传文件");
        }
        List<AdoptQuestion> questions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                // 去除 UTF-8 BOM
                if (isFirstLine) {
                    isFirstLine = false;
                    if (line.length() > 0 && line.charAt(0) == '﻿') {
                        line = line.substring(1);
                    }
                    if (line.contains("题目") || line.contains("question")) continue;
                }
                line = line.trim();
                if (line.isEmpty()) continue;

                // 支持 CSV 格式: 题目,选项A,选项B,选项C,选项D,正确答案
                // 使用简单的状态机解析，处理引号包裹的含逗号字段
                String[] parts = parseCsvLine(line);
                if (parts.length < 6) continue;

                AdoptQuestion q = new AdoptQuestion();
                q.setQuestion(parts[0].trim());
                q.setOptionA(parts[1].trim());
                q.setOptionB(parts[2].trim());
                q.setOptionC(parts[3].trim());
                q.setOptionD(parts[4].trim());
                q.setCorrectAnswer(parts[5].trim().toUpperCase());
                questions.add(q);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            return Result.error(ResultCodeEnum.UNKNOWN_ERROR, "导入失败，请检查文件格式");
        }

        if (questions.isEmpty()) {
            return Result.error(ResultCodeEnum.PARAM_INVALID, "未能从文件中解析到有效数据");
        }
        int count = questionService.batchImport(questions);
        return Result.success("成功导入 " + count + " 道试题");
    }

    /**
     * 简单 CSV 行解析：支持引号包裹的字段（含逗号），处理引号转义
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++; // skip escaped quote
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(field.toString());
                    field.setLength(0);
                } else {
                    field.append(c);
                }
            }
        }
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }

    private List<Long> extractLongList(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "缺少参数: " + key);
        }
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .map(item -> item instanceof Number ? ((Number) item).longValue() : Long.valueOf(item.toString()))
                    .collect(Collectors.toList());
        }
        throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "参数格式错误，应为数组: " + key);
    }

    /**
     * 所有领养申请列表（分页）
     */
    @ApiOperation("所有领养申请列表（管理员，分页）")
    @GetMapping("/applications")
    public Result<PageInfo<AdoptApplyVo>> applications(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<AdoptApplyVo> list = adoptService.getAllApplications(status, page, size);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 申请详情
     */
    @ApiOperation("申请详情（管理员）")
    @GetMapping("/applications/{id}")
    public Result<com.pet.module.adopt.model.vo.AdoptApplyVo> applicationDetail(@PathVariable Long id) {
        return Result.success(adoptService.getAdminApplicationDetail(id));
    }

    /**
     * 干预审核（通过/拒绝）
     */
    @ApiOperation("干预审核（管理员）")
    @PutMapping("/applications/{id}")
    public Result<String> review(@PathVariable Long id, @RequestParam String action) {
        adoptService.adminReview(id, action);
        return Result.success("审核完成");
    }
}