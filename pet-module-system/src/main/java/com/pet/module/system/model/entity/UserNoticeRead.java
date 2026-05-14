package com.pet.module.system.model.entity;

import java.time.LocalDateTime;

/**
 * 用户公告已读记录表 user_notice_read
 */
public class UserNoticeRead {

    private Long id;
    private Long userId;
    private Long noticeId;
    private LocalDateTime readAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getNoticeId() { return noticeId; }
    public void setNoticeId(Long noticeId) { this.noticeId = noticeId; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
