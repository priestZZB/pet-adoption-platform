package com.pet.module.system.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.module.system.service.CaptchaService;
import com.pet.module.system.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信验证码控制器
 */
@Log("短信验证码")
@Api(tags = "短信验证码")
@RestController
@RequestMapping("/api/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private CaptchaService captchaService;

    /**
     * 发送短信验证码
     *
     * @param dto { phone, ticket, randstr, captchaSign }
     * @return 验证码（调试时返回，测试用）
     */
    @ApiOperation("发送短信验证码")
    @PostMapping("/code")
    public Result<Map<String, String>> sendCode(@RequestBody SmsCodeDto dto) {
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            return Result.error(400, "手机号不能为空");
        }

        // 行为验证码校验（防短信轰炸）
        if (dto.getTicket() == null || dto.getTicket().isEmpty()) {
            return Result.error(400, "请先完成滑块验证");
        }
        if (!captchaService.verify(dto.getTicket(), dto.getRandstr(), dto.getCaptchaSign(), null)) {
            return Result.error(400, "滑块验证码验证失败，请重试");
        }

        String code = smsService.sendCode(dto.getPhone());
        Map<String, String> data = new HashMap<>();
        data.put("phone", dto.getPhone());
        data.put("code", code);  // 调试返回验证码，生产环境建议去掉
        // data.put("message", "验证码已发送");
        return Result.success("验证码已发送", data);
    }

    /**
     * 发送短信验证码请求参数
     */
    public static class SmsCodeDto {
        private String phone;
        /** 行为验证码票据 */
        private String ticket;
        /** 行为验证码随机字符串 */
        private String randstr;
        /** 行为验证码鉴权签名 */
        private String captchaSign;

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getTicket() { return ticket; }
        public void setTicket(String ticket) { this.ticket = ticket; }
        public String getRandstr() { return randstr; }
        public void setRandstr(String randstr) { this.randstr = randstr; }
        public String getCaptchaSign() { return captchaSign; }
        public void setCaptchaSign(String captchaSign) { this.captchaSign = captchaSign; }
    }
}
