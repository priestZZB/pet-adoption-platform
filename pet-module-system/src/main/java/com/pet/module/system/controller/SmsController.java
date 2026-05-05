package com.pet.module.system.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
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

    /**
     * 发送短信验证码
     *
     * @param dto { phone: "138xxxx" }
     * @return 验证码（调试时返回，测试用）
     */
    @ApiOperation("发送短信验证码")
    @PostMapping("/code")
    public Result<Map<String, String>> sendCode(@RequestBody SmsCodeDto dto) {
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            return Result.error(400, "手机号不能为空");
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

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
