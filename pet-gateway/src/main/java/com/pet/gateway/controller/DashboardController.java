package com.pet.gateway.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.system.mapper.FeedbackMapper;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.pet.mapper.PetInfoMapper;
import com.pet.module.mall.mapper.MallOrderMapper;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private MallOrderMapper mallOrderMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ApiOperation("控制台统计数据")
    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> dashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 头部卡片统计
        int userCount = userMapper.countAll();
        int petCount = petInfoMapper.countAll();
        int orderCount = mallOrderMapper.countAll();

        // 各类待审核
        int volunteerPending = userMapper.countByVolunteerStatus("PENDING");
        int donorPending = userMapper.countByDonorStatus("PENDING");
        int petFinalReview = petInfoMapper.countByStatus("FIRST_PASS");
        int feedbackPending = feedbackMapper.countPending();

        int pendingTotal = volunteerPending + donorPending + petFinalReview + feedbackPending;

        stats.put("userCount", userCount);
        stats.put("petCount", petCount);
        stats.put("orderCount", orderCount);
        stats.put("pendingTotal", pendingTotal);

        // 待审核明细
        Map<String, Integer> pending = new HashMap<>();
        pending.put("volunteerApplies", volunteerPending);
        pending.put("donorApplies", donorPending);
        pending.put("petFinalReview", petFinalReview);
        pending.put("feedbackPending", feedbackPending);
        pending.put("total", pendingTotal);
        stats.put("pending", pending);

        // 图表数据
        stats.put("chart", getChartData());

        return Result.success(stats);
    }

    /**
     * 近 14 天趋势数据
     */
    @ApiOperation("趋势图表数据")
    @GetMapping("/dashboard/chart")
    public Result<Map<String, Object>> chart() {
        return Result.success(getChartData());
    }

    private Map<String, Object> getChartData() {
        Map<String, Object> chart = new HashMap<>();

        // 近 14 天日期
        String[] labels = new String[14];
        long[] users = new long[14];
        long[] pets = new long[14];
        long[] orders = new long[14];

        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 13; i >= 0; i--) {
            java.time.LocalDate day = today.minusDays(i);
            labels[13 - i] = day.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd"));
            String dateStr = day.toString();
            try {
                users[13 - i] = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM sys_user WHERE DATE(created_at) = ?", Long.class, dateStr);
                pets[13 - i] = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM pet_info WHERE DATE(created_at) = ?", Long.class, dateStr);
                orders[13 - i] = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM mall_order WHERE DATE(created_at) = ?", Long.class, dateStr);
            } catch (Exception ignored) {}
        }

        chart.put("labels", labels);
        chart.put("users", users);
        chart.put("pets", pets);
        chart.put("orders", orders);
        return chart;
    }
}
