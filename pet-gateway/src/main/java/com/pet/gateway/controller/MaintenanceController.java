package com.pet.gateway.controller;

import com.pet.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 维护模式状态接口
 * 前端维护页面轮询此接口判断维护是否结束
 */
@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    @Value("${pet.maintenance.enabled:false}")
    private boolean maintenanceEnabled;

    @GetMapping("/status")
    public Result<Map<String, Boolean>> status() {
        Map<String, Boolean> data = new HashMap<>();
        data.put("maintenance", maintenanceEnabled);
        return Result.success(data);
    }
}
