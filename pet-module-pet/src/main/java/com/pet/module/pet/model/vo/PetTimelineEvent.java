package com.pet.module.pet.model.vo;

import java.time.LocalDateTime;

/**
 * 宠物事件时间线（用于送养人详情页）
 * 类型: PUBLISH / EDIT / FIRST_REVIEW / FINAL_REVIEW
 */
public class PetTimelineEvent {

    private String type;        // PUBLISH / EDIT / FIRST_REVIEW / FINAL_REVIEW
    private String action;      // 对于审核: APPROVED / REJECTED
    private String remark;      // 打回原因
    private String reviewerType; // 志愿者 / 管理员
    private LocalDateTime createdAt;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getReviewerType() { return reviewerType; }
    public void setReviewerType(String reviewerType) { this.reviewerType = reviewerType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
