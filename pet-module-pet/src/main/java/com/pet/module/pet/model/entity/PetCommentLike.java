package com.pet.module.pet.model.entity;

import java.time.LocalDateTime;

/**
 * 评论点赞表 pet_comment_like
 */
public class PetCommentLike {

    private Long id;
    private Long commentId;
    private Long userId;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
