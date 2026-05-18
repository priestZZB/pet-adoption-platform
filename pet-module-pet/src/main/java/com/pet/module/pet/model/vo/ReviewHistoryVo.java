package com.pet.module.pet.model.vo;

import java.time.LocalDateTime;

/**
 * 审核历史记录 VO
 * 每个志愿者对每只宠物只取最新一条审核记录
 */
public class ReviewHistoryVo {

    private Long reviewId;
    private Long petId;
    private String name;
    private String coverImage;
    private String categoryName;
    private String age;
    private String gender;
    private String myAction;       // APPROVED / REJECTED
    private String myRemark;
    private String reviewType;     // FIRST / FINAL
    private LocalDateTime reviewedAt;

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getMyAction() { return myAction; }
    public void setMyAction(String myAction) { this.myAction = myAction; }
    public String getMyRemark() { return myRemark; }
    public void setMyRemark(String myRemark) { this.myRemark = myRemark; }
    public String getReviewType() { return reviewType; }
    public void setReviewType(String reviewType) { this.reviewType = reviewType; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
