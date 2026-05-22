package com.pet.module.pet.model.vo;

import java.time.LocalDateTime;
import java.util.List;

public class CommentVo {

    private Long id;
    private Long petId;
    private Long userId;
    private String nickname;
    private String avatar;
    private Long parentId;
    private Long replyTo;
    private String replyToName;
    private String content;
    private List<String> images;
    private Integer likeCount;
    private Integer dislikeCount;
    private boolean liked;
    private boolean disliked;
    private Integer status;
    private LocalDateTime createdAt;
    private List<CommentVo> replies;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getReplyTo() { return replyTo; }
    public void setReplyTo(Long replyTo) { this.replyTo = replyTo; }
    public String getReplyToName() { return replyToName; }
    public void setReplyToName(String replyToName) { this.replyToName = replyToName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(Integer dislikeCount) { this.dislikeCount = dislikeCount; }
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public boolean isDisliked() { return disliked; }
    public void setDisliked(boolean disliked) { this.disliked = disliked; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<CommentVo> getReplies() { return replies; }
    public void setReplies(List<CommentVo> replies) { this.replies = replies; }
}
