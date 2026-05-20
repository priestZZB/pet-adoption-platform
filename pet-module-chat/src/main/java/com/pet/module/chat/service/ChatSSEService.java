package com.pet.module.chat.service;

import com.pet.module.chat.model.entity.ChatMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE（Server-Sent Events）实时推送服务
 * 管理用户 SSE 连接，推送聊天消息和未读数更新
 * 支持同一用户多个连接（Navbar + ChatDetail 等）
 */
@Service
public class ChatSSEService {

    /** userId → [SseEmitter, ...] 支持同一用户多个连接 */
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * 为用户创建 SSE 连接
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        CopyOnWriteArrayList<SseEmitter> list = emitters.computeIfAbsent(userId,
                k -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        emitter.onCompletion(() -> {
            list.remove(emitter);
            if (list.isEmpty()) { emitters.remove(userId); pushOnlineStatus(userId, false); }
        });
        emitter.onTimeout(() -> {
            list.remove(emitter);
            if (list.isEmpty()) { emitters.remove(userId); pushOnlineStatus(userId, false); }
        });
        emitter.onError(e -> {
            list.remove(emitter);
            if (list.isEmpty()) { emitters.remove(userId); pushOnlineStatus(userId, false); }
        });

        // 用户上线 + 推送当前所有在线用户给新连接
        pushOnlineStatus(userId, true);
        pushCurrentOnlineUsers(userId);

        return emitter;
    }

    /**
     * 推送新消息给接收者（同时确认发送者在线）
     */
    public void pushNewMessage(Long receiverId, ChatMessage msg) {
        pushToUser(receiverId, "new-message", msg);
        // 发消息 = 在线信号
        pushOnlineStatus(msg.getSenderId(), true);
    }

    /**
     * 推送自定义事件
     */
    public void pushEvent(Long userId, String eventName, String data) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
        if (list == null || list.isEmpty()) return;

        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                list.remove(emitter);
            }
        }
        if (list.isEmpty()) emitters.remove(userId);
    }

    /**
     * 推送未读数更新给指定用户
     */
    public void pushUnreadCount(Long userId, int count) {
        pushToUser(userId, "unread-count", "{\"count\": " + count + "}");
    }

    /**
     * 推送会话列表更新
     */
    public void pushConversationUpdate(Long userId) {
        pushToUser(userId, "conversation-update", "{}");
    }

    /**
     * 获取当前所有在线用户 ID
     */
    public Set<Long> getOnlineUserIds() {
        return emitters.keySet();
    }

    /**
     * 断开用户的所有 SSE 连接
     */
    public void unsubscribe(Long userId) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.remove(userId);
        if (list != null) {
            for (SseEmitter emitter : list) {
                try { emitter.complete(); } catch (Exception ignored) {}
            }
        }
    }

    // ---- 内部辅助 ----

    /**
     * 推送用户在线状态变化给所有连接
     */
    private void pushOnlineStatus(Long userId, boolean online) {
        String data = "{\"userId\":" + userId + ",\"online\":" + online + "}";
        for (Long uid : emitters.keySet()) {
            pushToUser(uid, "online-status", data);
        }
    }

    /**
     * 推送当前所有在线用户列表给指定用户
     */
    private void pushCurrentOnlineUsers(Long userId) {
        for (Long uid : emitters.keySet()) {
            if (!uid.equals(userId)) {
                pushToUser(userId, "online-status",
                    "{\"userId\":" + uid + ",\"online\":true}");
            }
        }
    }

    private void pushToUser(Long userId, String eventName, Object data) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
        if (list == null || list.isEmpty()) return;

        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                list.remove(emitter);
            }
        }
        if (list.isEmpty()) emitters.remove(userId);
    }
}
