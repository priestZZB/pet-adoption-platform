package com.pet.module.pet.model.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物详情返回（含多图+送养人信息）
 */
public class PetDetailVo {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String userPhone;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String age;
    private String gender;
    private Integer isNeutered;
    private Integer isVaccinated;
    private String healthCert;
    private String personality;
    private String habit;
    private String reason;
    private String status;
    private String reviewRemark;
    private List<String> images;
    private Integer favoriteCount;
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
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getIsNeutered() { return isNeutered; }
    public void setIsNeutered(Integer isNeutered) { this.isNeutered = isNeutered; }
    public Integer getIsVaccinated() { return isVaccinated; }
    public void setIsVaccinated(Integer isVaccinated) { this.isVaccinated = isVaccinated; }
    public String getHealthCert() { return healthCert; }
    public void setHealthCert(String healthCert) { this.healthCert = healthCert; }
    public String getPersonality() { return personality; }
    public void setPersonality(String personality) { this.personality = personality; }
    public String getHabit() { return habit; }
    public void setHabit(String habit) { this.habit = habit; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}