package com.pet.common.event;

/**
 * 站内通知事件
 * 审核/审批操作完成后发布此事件，由监听器统一写入通知表
 */
public class NotificationEvent {

    /** 接收通知的用户ID */
    private Long userId;

    /** 通知类型：VOLUNTEER_REVIEW / DONOR_REVIEW / PET_REVIEW / ADOPT_REVIEW */
    private String type;

    /** 通知标题（如"送养人申请已通过"） */
    private String title;

    /** 通知内容 */
    private String content;

    /** 关联的业务ID（如宠物ID、申请ID等，用于前端跳转） */
    private Long relatedId;

    public NotificationEvent() {}

    public NotificationEvent(Long userId, String type, String title, String content, Long relatedId) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.relatedId = relatedId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
}
