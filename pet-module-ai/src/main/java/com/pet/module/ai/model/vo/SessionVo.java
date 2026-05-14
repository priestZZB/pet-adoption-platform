package com.pet.module.ai.model.vo;

import java.time.LocalDateTime;

/**
 * 对话摘要（侧边栏用）
 */
public class SessionVo {

    private String sessionId;
    private Long userId;
    private String username;
    private String title;       // 第一句话作为标题
    private int msgCount;       // 问答次数
    private LocalDateTime createdAt;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getMsgCount() { return msgCount; }
    public void setMsgCount(int msgCount) { this.msgCount = msgCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
