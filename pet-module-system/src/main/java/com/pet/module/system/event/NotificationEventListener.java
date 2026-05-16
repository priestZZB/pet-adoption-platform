package com.pet.module.system.event;

import com.pet.common.event.NotificationEvent;
import com.pet.module.system.mapper.NotificationMapper;
import com.pet.module.system.model.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 站内通知事件监听器
 * 异步处理通知写入，不阻塞主业务流程
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    @Autowired
    private NotificationMapper notificationMapper;

    @Async
    @EventListener
    @Transactional
    public void handleNotification(NotificationEvent event) {
        try {
            Notification n = new Notification();
            n.setUserId(event.getUserId());
            n.setType(event.getType());
            n.setTitle(event.getTitle());
            n.setContent(event.getContent());
            n.setRelatedId(event.getRelatedId());
            n.setIsRead(0);
            notificationMapper.insert(n);
            log.debug("站内通知已发送: userId={}, type={}, title={}", event.getUserId(), event.getType(), event.getTitle());
        } catch (Exception e) {
            log.warn("站内通知发送失败: {}", e.getMessage());
        }
    }
}
