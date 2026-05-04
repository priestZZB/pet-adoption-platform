package com.pet.module.pet.model.entity;

import java.time.LocalDateTime;

/**
 * 宠物信息表 pet_info
 */
public class PetInfo {

    private Long id;
    private Long userId;
    private Long categoryId;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}