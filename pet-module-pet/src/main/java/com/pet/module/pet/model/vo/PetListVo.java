package com.pet.module.pet.model.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 宠物列表返回
 */
public class PetListVo implements Serializable {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String age;
    private String gender;
    private Integer isNeutered;
    private Integer isVaccinated;
    private String coverImage;
    private String status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
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
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}