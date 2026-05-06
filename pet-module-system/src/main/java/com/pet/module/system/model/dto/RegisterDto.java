package com.pet.module.system.model.dto;

/**
 * 注册请求
 */
public class RegisterDto {

    private String username;
    private String password;
    private String nickname;
    private String phone;
    /** 短信验证码 */
    private String smsCode;
    /** 行为验证码票据（滑块验证码回调） */
    private String ticket;
    /** 行为验证码随机字符串 */
    private String randstr;
    /** 行为验证码鉴权签名 */
    private String captchaSign;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSmsCode() { return smsCode; }
    public void setSmsCode(String smsCode) { this.smsCode = smsCode; }
    public String getTicket() { return ticket; }
    public void setTicket(String ticket) { this.ticket = ticket; }
    public String getRandstr() { return randstr; }
    public void setRandstr(String randstr) { this.randstr = randstr; }
    public String getCaptchaSign() { return captchaSign; }
    public void setCaptchaSign(String captchaSign) { this.captchaSign = captchaSign; }
}