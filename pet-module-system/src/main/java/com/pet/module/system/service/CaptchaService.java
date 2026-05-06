package com.pet.module.system.service;

/**
 * 行为验证码服务（怜花滑块验证码）
 * 用于登录、注册、找回密码等敏感操作的人机验证
 */
public interface CaptchaService {

    /**
     * 验证滑块验证码
     *
     * @param ticket       前端回调票据
     * @param randstr      加密随机字符串
     * @param captchaSign  行为验证码鉴权签名
     * @param sourceIp     触发IP（可选，传null可跳过IP校验）
     * @return true=验证通过
     */
    boolean verify(String ticket, String randstr, String captchaSign, String sourceIp);
}
