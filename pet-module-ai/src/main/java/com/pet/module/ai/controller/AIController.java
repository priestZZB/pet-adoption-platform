package com.pet.module.ai.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.ai.model.dto.ChatDto;
import com.pet.module.ai.model.entity.AIConversation;
import com.pet.module.ai.model.vo.ChatVo;
import com.pet.module.ai.model.vo.SessionVo;
import com.pet.module.ai.service.AIService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Log("AI咨询")
@Api(tags = "AI咨询")
@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @ApiOperation("AI对话")
    @PostMapping("/chat")
    public Result<ChatVo> chat(HttpServletRequest request, @RequestBody ChatDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(aiService.chat(userId, dto));
    }

    @ApiOperation("对话历史（分页，兼容旧版）")
    @GetMapping("/history")
    public Result<PageInfo<ChatVo>> history(HttpServletRequest request,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        List<ChatVo> list = aiService.getHistory(userId, page, size);
        return Result.success(new PageInfo<>(list));
    }

    @ApiOperation("清空对话历史")
    @DeleteMapping("/history")
    public Result<String> clearHistory(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        aiService.clearHistory(userId);
        return Result.success("已清空");
    }

    @ApiOperation("获取对话列表（按 session 分组）")
    @GetMapping("/sessions")
    public Result<List<SessionVo>> sessions(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(aiService.getSessions(userId));
    }

    @ApiOperation("获取某次对话的消息列表（用户侧，过滤已删除）")
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ChatVo>> sessionMessages(@PathVariable String sessionId) {
        return Result.success(aiService.getSessionMessages(sessionId, false));
    }

    @ApiOperation("获取某次对话的消息列表（管理员，包含已删除）")
    @GetMapping("/admin/sessions/{sessionId}/messages")
    @RequireRole("ADMIN")
    public Result<List<ChatVo>> adminSessionMessages(@PathVariable String sessionId) {
        return Result.success(aiService.getSessionMessages(sessionId, true));
    }

    @ApiOperation("删除某次对话")
    @DeleteMapping("/sessions/{sessionId}")
    public Result<String> deleteSession(HttpServletRequest request, @PathVariable String sessionId) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        aiService.deleteSession(userId, sessionId);
        return Result.success("已删除");
    }

    @ApiOperation("所有对话列表（管理员）")
    @GetMapping("/admin/sessions")
    @RequireRole("ADMIN")
    public Result<List<SessionVo>> adminSessions() {
        return Result.success(aiService.getAllSessions());
    }

    @ApiOperation("所有AI问答记录（管理员，分页）")
    @GetMapping("/admin/records")
    @RequireRole("ADMIN")
    public Result<PageInfo<AIConversation>> allRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(aiService.getAllRecords(page, size));
    }
}
