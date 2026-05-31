package com.pet.module.chat.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.common.event.NotificationEvent;
import com.pet.module.chat.mapper.ChatMessageMapper;
import com.pet.module.chat.service.ChatSSEService;
import com.pet.module.system.mapper.NotificationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 站内通知 SSE 推送监听器
 * 当有新通知时通过 SSE 实时推送给用户
 */
@Component
public class ChatNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(ChatNotificationListener.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChatSSEService chatSSEService;

    @Autowired(required = false)
    private NotificationMapper notificationMapper;

    @EventListener
    public void handleNotification(NotificationEvent event) {
        try {
            // 推送通知事件
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", event.getType());
            payload.put("title", event.getTitle());
            payload.put("content", event.getContent());
            payload.put("relatedId", event.getRelatedId());
            String data = objectMapper.writeValueAsString(payload);
            chatSSEService.pushEvent(event.getUserId(), "new-notification", data);

            // 推送未读数更新
            if (notificationMapper != null) {
                int count = notificationMapper.countUnreadByUserId(event.getUserId());
                chatSSEService.pushUnreadCount(event.getUserId(), count);
            }
        } catch (Exception e) {
            log.warn("SSE 推送通知失败: {}", e.getMessage());
        }
    }
}
