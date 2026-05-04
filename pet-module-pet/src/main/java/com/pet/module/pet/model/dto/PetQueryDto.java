package com.pet.module.pet.model.dto;

/**
 * 宠物查询条件
 */
public class PetQueryDto {

    private Long categoryId;
    private String keyword;
    private String status;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}