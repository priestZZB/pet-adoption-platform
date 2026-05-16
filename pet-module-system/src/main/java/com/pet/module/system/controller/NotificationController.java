package com.pet.module.system.controller;

import com.pet.common.result.Result;
import com.pet.module.system.mapper.NotificationMapper;
import com.pet.module.system.model.entity.Notification;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内通知
 */
@Api(tags = "站内通知")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 我的通知列表（按时间倒序）
     */
    @ApiOperation("我的通知列表")
    @GetMapping
    public Result<List<Notification>> myNotifications(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(notificationMapper.selectByUserId(userId));
    }

    /**
     * 未读通知数量
     */
    @ApiOperation("未读通知数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> unreadCount(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        int count = notificationMapper.countUnreadByUserId(userId);
        Map<String, Integer> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    /**
     * 标记单条通知为已读
     */
    @ApiOperation("标记已读")
    @PutMapping("/{id}/read")
    public Result<String> markRead(HttpServletRequest request, @PathVariable Long id) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        notificationMapper.markAsRead(id, userId);
        return Result.success("已标记");
    }

    /**
     * 全部标记已读
     */
    @ApiOperation("全部标记已读")
    @PutMapping("/read-all")
    public Result<String> markAllRead(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        notificationMapper.markAllAsRead(userId);
        return Result.success("已全部标记");
    }
}
