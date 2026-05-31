package com.pet.gateway.controller;

import com.pet.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 公共配置接口
 * 前端通过此接口获取服务端配置（如mock开关），统一由后端控制
 */
@RestController
public class PublicConfigController {

    @Value("${pet.captcha.mock:false}")
    private boolean captchaMock;

    @Value("${pet.sms.mock:false}")
    private boolean smsMock;

    @Value("${pet.realname.mock:false}")
    private boolean realnameMock;

    @Value("${pet.ai.mock:false}")
    private boolean aiMock;

    @Value("${pet.map.mock:false}")
    private boolean mapMock;

    @GetMapping("/api/public/config")
    public Result<Map<String, Boolean>> getConfig() {
        Map<String, Boolean> config = new HashMap<>();
        config.put("captchaMock", captchaMock);
        config.put("smsMock", smsMock);
        config.put("realnameMock", realnameMock);
        config.put("aiMock", aiMock);
        config.put("mapMock", mapMock);
        return Result.success(config);
    }
}
