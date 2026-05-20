package com.pet.module.chat.controller;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.util.JwtUtils;
import com.pet.module.chat.model.entity.ChatMessage;
import com.pet.module.chat.service.ChatSSEService;
import com.pet.module.chat.service.ChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log("聊天")
@Api(tags = "聊天")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatSSEService chatSSEService;

    @Autowired
    private JwtUtils jwtUtils;

    @ApiOperation("发送消息")
    @PostMapping("/send")
    public Result<Map<String, Object>> send(HttpServletRequest request,
                                            @RequestParam Long receiverId,
                                            @RequestParam Long petId,
                                            @RequestParam(required = false) String content,
                                            @RequestParam(required = false) String imageUrl) {
        Long senderId = Long.valueOf(request.getAttribute("userId").toString());
        if ((content == null || content.trim().isEmpty()) && (imageUrl == null || imageUrl.isEmpty())) {
            return Result.error(com.pet.common.enums.ResultCodeEnum.BAD_REQUEST, "请输入内容或上传图片");
        }
        Long msgId = chatService.sendMessage(senderId, receiverId, petId, content, imageUrl);
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

    @ApiOperation("删除某个会话的所有消息")
    @DeleteMapping("/conversation")
    public Result<String> deleteConversation(HttpServletRequest request,
                                              @RequestParam Long petId,
                                              @RequestParam Long otherUserId) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        chatService.deleteConversation(petId, userId, otherUserId);
        return Result.success("已删除");
    }

    @ApiOperation("在线用户列表")
    @GetMapping("/online-users")
    public Result<Set<Long>> onlineUsers() {
        return Result.success(chatSSEService.getOnlineUserIds());
    }

    @ApiOperation("SSE 实时消息订阅")
    @GetMapping("/sse/subscribe")
    public SseEmitter subscribe(@RequestParam(required = false) String token,
                                HttpServletRequest request) {
        // 优先从 query param 取 token（EventSource 无法设置 Header）
        // token 无效时返回带错误事件的 emitter 而非抛异常（避免 SSE 媒体类型序列化失败）
        if (token == null || token.isEmpty()) {
            return createErrorEmitter("token_missing", "未提供认证令牌");
        }
        Claims claims;
        try {
            claims = jwtUtils.parseToken(token);
        } catch (Exception e) {
            return createErrorEmitter("token_invalid", "令牌无效或已过期");
        }
        Long userId = Long.valueOf(claims.getSubject());
        return chatSSEService.subscribe(userId);
    }

    /**
     * 创建一个发送完错误事件后自动关闭的 SSE Emitter
     */
    private SseEmitter createErrorEmitter(String eventName, String message) {
        SseEmitter emitter = new SseEmitter(1000L); // 1秒超时
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"code\":\"" + eventName + "\",\"message\":\"" + message + "\"}"));
        } catch (IOException ignored) {}
        emitter.complete();
        return emitter;
    }
}
