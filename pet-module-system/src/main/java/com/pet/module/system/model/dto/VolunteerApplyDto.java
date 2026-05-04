package com.pet.module.system.model.dto;

/**
 * 志愿者申请审核请求
 */
public class VolunteerApplyDto {

    private String action;
    private String remark;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}