package com.pet.module.chat.event;

import com.pet.common.event.NotificationEvent;
import com.pet.module.chat.mapper.ChatMessageMapper;
import com.pet.module.chat.service.ChatSSEService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 站内通知 SSE 推送监听器
 * 当有新通知时通过 SSE 实时推送给用户
 */
@Component
public class ChatNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(ChatNotificationListener.class);

    @Autowired
    private ChatSSEService chatSSEService;

    @EventListener
    public void handleNotification(NotificationEvent event) {
        try {
            // 推送通知事件
            chatSSEService.pushEvent(event.getUserId(), "new-notification",
                    "{\"type\":\"" + event.getType() + "\",\"title\":\"" +
                    escapeJson(event.getTitle()) + "\",\"content\":\"" +
                    escapeJson(event.getContent()) + "\",\"relatedId\":" +
                    (event.getRelatedId() != null ? event.getRelatedId() : "null") + "}");

            // 推送未读数更新
            // 未读数由前端自己重新拉取
        } catch (Exception e) {
            log.warn("SSE 推送通知失败: {}", e.getMessage());
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
