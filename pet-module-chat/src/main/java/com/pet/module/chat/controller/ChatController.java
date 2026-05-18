package com.pet.module.chat.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.module.chat.model.entity.ChatMessage;
import com.pet.module.chat.service.ChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log("聊天")
@Api(tags = "聊天")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @ApiOperation("发送消息")
    @PostMapping("/send")
    public Result<Map<String, Object>> send(HttpServletRequest request,
                                            @RequestParam Long receiverId,
                                            @RequestParam Long petId,
                                            @RequestParam String content) {
        Long senderId = Long.valueOf(request.getAttribute("userId").toString());
        Long msgId = chatService.sendMessage(senderId, receiverId, petId, content);
        Map<String, Object> data = new HashMap<>();
        data.put("id", msgId);
        return Result.success(data);
    }

    @ApiOperation("获取会话消息")
    @GetMapping("/conversation")
    public Result<List<ChatMessage>> conversation(HttpServletRequest request,
                                                   @RequestParam Long petId,
                                                   @RequestParam Long otherUserId) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(chatService.getConversation(petId, userId, otherUserId));
    }

    @ApiOperation("我的会话列表")
    @GetMapping("/conversations")
    public Result<List<ChatMessage>> conversations(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(chatService.getMyConversations(userId));
    }

    @ApiOperation("未读消息数")
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> unreadCount(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        Map<String, Integer> data = new HashMap<>();
        data.put("count", chatService.getUnreadCount(userId));
        return Result.success(data);
    }

    @ApiOperation("标记已读")
    @PutMapping("/read")
    public Result<String> markRead(HttpServletRequest request,
                                   @RequestParam Long petId,
                                   @RequestParam Long otherUserId) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        chatService.markAsRead(petId, otherUserId, userId);
        return Result.success("ok");
    }
}
