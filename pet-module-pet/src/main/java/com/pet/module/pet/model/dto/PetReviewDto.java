package com.pet.module.pet.model.dto;

/**
 * 审核请求
 */
public class PetReviewDto {

    private String action;
    private String remark;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}