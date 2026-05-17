package com.pet.module.system.model.dto;

/**
 * 修改个人信息请求
 */
public class UserUpdateDto {

    private String nickname;
    private String email;
    private String phone;
    /** 修改手机号时需要短信验证码 */
    private String smsCode;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSmsCode() { return smsCode; }
    public void setSmsCode(String smsCode) { this.smsCode = smsCode; }
}
