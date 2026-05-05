package com.pet.module.system.service;

/**
 * 短信服务（三网106短信验证码）
 */
public interface SmsService {

    /**
     * 发送短信验证码
     *
     * @param phone 目标手机号
     * @return 验证码（可用于直接返回给前端调试，生产环境应隐藏）
     */
    String sendCode(String phone);

    /**
     * 校验短信验证码
     *
     * @param phone 手机号
     * @param code  用户输入的验证码
     * @return true=校验通过
     */
    boolean verifyCode(String phone, String code);
}
