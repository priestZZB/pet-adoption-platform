package com.pet.module.chat.controller;

import com.pet.module.chat.service.ChatSSEService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket STOMP 消息处理器
 * 处理打字提示、在线状态等实时事件
 */
@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatSSEService chatSSEService;

    /** 在线用户集合 */
    private final Set<Long> onlineUsers = ConcurrentHashMap.newKeySet();

    /**
     * 打字提示：用户 A 正在输入 → 推送给用户 B
     * 前端发送: /app/chat/typing
     */
    @MessageMapping("/chat/typing")
    public void handleTyping(@Payload Map<String, Object> payload) {
        Object senderObj = payload.get("senderId");
        Object receiverObj = payload.get("receiverId");
        if (senderObj == null || receiverObj == null) return;

        Long senderId = Long.valueOf(senderObj.toString());
        Long receiverId = Long.valueOf(receiverObj.toString());

        messagingTemplate.convertAndSendToUser(
                receiverId.toString(), "/queue/typing",
                "{\"senderId\":" + senderId + "}");
    }

    /**
     * 用户上线
     * 前端发送: /app/chat/online
     */
    @MessageMapping("/chat/online")
    public void handleOnline(@Payload Map<String, Object> payload) {
        Object userIdObj = payload.get("userId");
        if (userIdObj == null) return;

        Long userId = Long.valueOf(userIdObj.toString());
        // Set.add() 返回 true 表示之前不在集合中（确实新增了）
        boolean isNewUser = onlineUsers.add(userId);

        if (isNewUser) {
            broadcastOnlineStatus(userId, true);
        }
    }

    /**
     * 用户下线
     * 前端发送: /app/chat/offline
     */
    @MessageMapping("/chat/offline")
    public void handleOffline(@Payload Map<String, Object> payload) {
        Object userIdObj = payload.get("userId");
        if (userIdObj == null) return;

        Long userId = Long.valueOf(userIdObj.toString());
        onlineUsers.remove(userId);

        broadcastOnlineStatus(userId, false);
    }

    /**
     * 查询用户是否在线
     */
    @MessageMapping("/chat/check-online")
    public void handleCheckOnline(@Payload Map<String, Object> payload) {
        Object userIdObj = payload.get("userId");
        Object checkUserIdObj = payload.get("checkUserId");
        if (userIdObj == null || checkUserIdObj == null) return;

        Long userId = Long.valueOf(userIdObj.toString());
        Long checkUserId = Long.valueOf(checkUserIdObj.toString());
        boolean online = onlineUsers.contains(checkUserId);

        messagingTemplate.convertAndSendToUser(
                userId.toString(), "/queue/online",
                "{\"userId\":" + checkUserId + ",\"online\":" + online + "}");
    }

    /**
     * 向所有在线用户广播某用户的在线/离线状态
     * @param userId 状态变化的用户ID
     * @param online true=上线 false=离线
     */
    private void broadcastOnlineStatus(Long userId, boolean online) {
        String data = "{\"userId\":" + userId + ",\"online\":" + online + "}";
        for (Long uid : onlineUsers) {
            if (!uid.equals(userId)) {
                messagingTemplate.convertAndSendToUser(
                        uid.toString(), "/queue/online", data);
            }
        }
    }

    /** 获取在线用户列表 */
    public Set<Long> getOnlineUsers() {
        return onlineUsers;
    }
}
