package com.pet.module.adopt.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.module.adopt.model.dto.ExamSubmitDto;
import com.pet.module.adopt.model.vo.ExamResultVo;
import com.pet.module.adopt.model.vo.QuestionVo;
import com.pet.module.adopt.service.ExamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Log("领养考试")
@Api(tags = "领养考试")
@RestController
@RequestMapping("/api/adopt/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    /**
     * 开始答题（随机10题）
     */
    @ApiOperation("开始答题（随机10题）")
    @GetMapping("/start")
    public Result<List<QuestionVo>> start() {
        return Result.success(examService.startExam());
    }

    /**
     * 提交答题 → 返回分数
     */
    @ApiOperation("提交答题")
    @PostMapping("/submit")
    public Result<ExamResultVo> submit(HttpServletRequest request, @RequestBody ExamSubmitDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(examService.submitExam(userId, dto));
    }

    /**
     * 历史答题记录
     */
    @ApiOperation("历史答题记录")
    @GetMapping("/history")
    public Result<List<ExamResultVo>> history(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(examService.getHistory(userId));
    }
}
