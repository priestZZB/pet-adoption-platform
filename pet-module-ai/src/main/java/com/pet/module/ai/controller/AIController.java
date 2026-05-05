package com.pet.module.ai.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.ai.model.dto.ChatDto;
import com.pet.module.ai.model.entity.AIConversation;
import com.pet.module.ai.model.vo.ChatVo;
import com.pet.module.ai.service.AIService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "AI咨询")
@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * AI对话（发送问题→返回回答）
     */
    @ApiOperation("AI对话")
    @PostMapping("/chat")
    public Result<ChatVo> chat(HttpServletRequest request, @RequestBody ChatDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(aiService.chat(userId, dto));
    }

    /**
     * 对话历史（分页）
     */
    @ApiOperation("对话历史")
    @GetMapping("/history")
    public Result<PageInfo<ChatVo>> history(HttpServletRequest request,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        List<ChatVo> list = aiService.getHistory(userId, page, size);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 清空对话历史
     */
    @ApiOperation("清空对话历史")
    @DeleteMapping("/history")
    public Result<String> clearHistory(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        aiService.clearHistory(userId);
        return Result.success("已清空");
    }

    /**
     * 所有AI问答记录（管理员）
     */
    @ApiOperation("所有AI问答记录（管理员）")
    @GetMapping("/admin/records")
    @RequireRole("ADMIN")
    public Result<List<AIConversation>> allRecords() {
        return Result.success(aiService.getAllRecords());
    }
}