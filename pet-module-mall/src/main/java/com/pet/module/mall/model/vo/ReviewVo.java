package com.pet.module.mall.model.vo;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewVo {

    private Long id;
    private Long productId;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer rating;
    private String content;
    private List<String> images;
    private List<String> tags;
    private Integer isAdditional;
    private LocalDateTime createdAt;
    private List<ReviewVo> additions;
    private String adminReply;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public Integer getIsAdditional() { return isAdditional; }
    public void setIsAdditional(Integer isAdditional) { this.isAdditional = isAdditional; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<ReviewVo> getAdditions() { return additions; }
    public void setAdditions(List<ReviewVo> additions) { this.additions = additions; }
    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }
}
