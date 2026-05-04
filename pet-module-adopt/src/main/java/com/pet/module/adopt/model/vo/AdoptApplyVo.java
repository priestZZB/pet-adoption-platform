package com.pet.module.adopt.model.vo;

import java.time.LocalDateTime;

/**
 * 领养申请返回
 */
public class AdoptApplyVo {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String userPhone;
    private Long petId;
    private String petName;
    private String petCoverImage;
    private String livingEnv;
    private String petExp;
    private String commitment;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getPetCoverImage() { return petCoverImage; }
    public void setPetCoverImage(String petCoverImage) { this.petCoverImage = petCoverImage; }
    public String getLivingEnv() { return livingEnv; }
    public void setLivingEnv(String livingEnv) { this.livingEnv = livingEnv; }
    public String getPetExp() { return petExp; }
    public void setPetExp(String petExp) { this.petExp = petExp; }
    public String getCommitment() { return commitment; }
    public void setCommitment(String commitment) { this.commitment = commitment; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}