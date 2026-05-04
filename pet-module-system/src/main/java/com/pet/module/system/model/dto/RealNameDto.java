package com.pet.module.system.model.dto;

/**
 * 实名认证请求
 */
public class RealNameDto {

    private String realName;
    private String idCard;
    private String phone;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}