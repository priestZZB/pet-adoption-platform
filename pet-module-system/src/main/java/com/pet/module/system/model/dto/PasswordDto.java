package com.pet.module.system.model.dto;

/**
 * 修改密码 / 找回密码请求
 */
public class PasswordDto {

    private String oldPassword;
    private String newPassword;
    /** 找回密码时用 */
    private String username;
    private String phone;
    /** 短信验证码（找回密码时用） */
    private String smsCode;

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSmsCode() { return smsCode; }
    public void setSmsCode(String smsCode) { this.smsCode = smsCode; }
}