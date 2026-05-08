package com.pet.module.system.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.system.model.entity.SysFeedback;
import com.pet.module.system.service.FeedbackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Log("意见反馈")
@Api(tags = "意见反馈")
@RestController
@RequestMapping
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 提交意见反馈
     */
    @ApiOperation("提交意见反馈")
    @PostMapping("/api/feedback")
    public Result<String> submit(HttpServletRequest request, @RequestBody FeedbackSubmitDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        feedbackService.submit(userId, dto.getContent(), dto.getImages());
        return Result.success("提交成功");
    }

    /**
     * 提交意见反馈请求参数
     */
    public static class FeedbackSubmitDto {
        private String content;
        private String images;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getImages() { return images; }
        public void setImages(String images) { this.images = images; }
    }

    /**
     * 我的反馈列表
     */
    @ApiOperation("我的反馈列表")
    @GetMapping("/api/feedback/my")
    public Result<List<SysFeedback>> myFeedbacks(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(feedbackService.getMyFeedbacks(userId));
    }

    /**
     * 所有反馈列表（管理员）
     */
    @ApiOperation("所有反馈列表（管理员）")
    @GetMapping("/api/admin/feedbacks")
    @RequireRole("ADMIN")
    public Result<List<SysFeedback>> allFeedbacks() {
        return Result.success(feedbackService.getAll());
    }

    /**
     * 回复反馈（管理员）
     */
    @ApiOperation("回复反馈（管理员）")
    @PostMapping("/api/admin/feedback/{id}/reply")
    @RequireRole("ADMIN")
    public Result<String> reply(@PathVariable Long id, @RequestParam String reply) {
        feedbackService.reply(id, reply);
        return Result.success("回复成功");
    }
}