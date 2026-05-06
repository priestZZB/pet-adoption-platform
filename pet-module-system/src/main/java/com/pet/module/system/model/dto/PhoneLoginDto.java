package com.pet.module.system.model.dto;

/**
 * 手机号+短信验证码登录请求
 */
public class PhoneLoginDto {

    private String phone;
    private String smsCode;
    /** 行为验证码票据（滑块验证码回调） */
    private String ticket;
    /** 行为验证码随机字符串 */
    private String randstr;
    /** 行为验证码鉴权签名 */
    private String captchaSign;

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
