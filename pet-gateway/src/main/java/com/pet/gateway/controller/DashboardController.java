package com.pet.gateway.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.pet.mapper.PetInfoMapper;
import com.pet.module.adopt.mapper.AdoptApplicationMapper;
import com.pet.module.mall.mapper.MallOrderMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 控制台 Dashboard 统计
 */
@Log("控制台")
@Api(tags = "管理员-控制台")
@RestController
@RequestMapping("/api/admin")
@RequireRole("ADMIN")
public class DashboardController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetInfoMapper petInfoMapper;

    @Autowired
    private AdoptApplicationMapper adoptApplicationMapper;

    @Autowired
    private MallOrderMapper mallOrderMapper;

    @ApiOperation("控制台统计数据")
    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> dashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 头部卡片统计
        int userCount = userMapper.countAll();
        int petCount = petInfoMapper.countAll();
        int adoptCount = adoptApplicationMapper.countAll();
        int orderCount = mallOrderMapper.countAll();

        // 各类待审核
        int volunteerPending = userMapper.countByVolunteerStatus("PENDING");
        int donorPending = userMapper.countByDonorStatus("PENDING");
        int petFirstReview = petInfoMapper.countByStatus("PENDING");
        int petFinalReview = petInfoMapper.countByStatus("FIRST_PASS");
        int adoptPending = adoptApplicationMapper.countByStatus("PENDING");

        int pendingTotal = volunteerPending + donorPending + petFirstReview
                          + petFinalReview + adoptPending;

        stats.put("userCount", userCount);
        stats.put("petCount", petCount);
        stats.put("adoptCount", adoptCount);
        stats.put("orderCount", orderCount);
        stats.put("pendingTotal", pendingTotal);

        // 待审核明细
        Map<String, Integer> pending = new HashMap<>();
        pending.put("volunteerApplies", volunteerPending);
        pending.put("donorApplies", donorPending);
        pending.put("petFirstReview", petFirstReview);
        pending.put("petFinalReview", petFinalReview);
        pending.put("adoptApplications", adoptPending);
        pending.put("total", pendingTotal);
        stats.put("pending", pending);

        return Result.success(stats);
    }
}
