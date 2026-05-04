package com.pet.module.adopt.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.adopt.model.entity.AdoptQuestion;
import com.pet.module.adopt.service.AdoptService;
import com.pet.module.adopt.service.QuestionService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @DeleteMapping("/questions/{id}")
    public Result<String> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 所有领养申请列表
     */
    @GetMapping("/applications")
    public Result<List<com.pet.module.adopt.model.vo.AdoptApplyVo>> applications(
            @RequestParam(required = false) String status) {
        return Result.success(adoptService.getAllApplications(status));
    }

    /**
     * 申请详情
     */
    @GetMapping("/applications/{id}")
    public Result<com.pet.module.adopt.model.vo.AdoptApplyVo> applicationDetail(@PathVariable Long id) {
        return Result.success(adoptService.getAdminApplicationDetail(id));
    }

    /**
     * 干预审核（通过/拒绝）
     */
    @PutMapping("/applications/{id}")
    public Result<String> review(@PathVariable Long id, @RequestParam String action) {
        adoptService.adminReview(id, action);
        return Result.success("审核完成");
    }
}