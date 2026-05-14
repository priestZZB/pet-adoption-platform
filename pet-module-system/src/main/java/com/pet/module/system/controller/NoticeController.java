package com.pet.module.system.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.system.model.entity.SysNotice;
import com.pet.module.system.model.vo.NoticeVo;
import com.pet.module.system.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log("公告管理")
@Api(tags = "公告管理")
@RestController
@RequestMapping
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 公告列表（公开）
     */
    @ApiOperation("公告列表（公开）")
    @GetMapping("/api/notices")
    public Result<List<NoticeVo>> list() {
        return Result.success(noticeService.getPublicNotices());
    }

    /**
     * 公告详情
     */
    @ApiOperation("公告详情")
    @GetMapping("/api/notices/{id}")
    public Result<NoticeVo> detail(@PathVariable Long id) {
        return Result.success(noticeService.getById(id));
    }

    /**
     * 未读公告列表（登录用户，管理员除外）
     */
    @ApiOperation("未读公告列表")
    @GetMapping("/api/notices/unread")
    public Result<List<NoticeVo>> unread(HttpServletRequest request) {
        Long userId = null;
        String role = null;
        try {
            userId = Long.valueOf(request.getAttribute("userId").toString());
            role = request.getAttribute("role").toString();
        } catch (Exception e) {
            return Result.success(List.of());
        }
        // 管理员不显示未读公告弹窗
        if ("ADMIN".equals(role)) {
            return Result.success(List.of());
        }
        return Result.success(noticeService.getUnreadNotices(userId));
    }

    /**
     * 标记公告为已读
     */
    @ApiOperation("标记公告为已读")
    @PostMapping("/api/notices/{id}/read")
    public Result<String> markRead(HttpServletRequest request, @PathVariable Long id) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        noticeService.markAsRead(userId, id);
        return Result.success("ok");
    }

    /**
     * 发布公告（管理员）
     */
    @ApiOperation("发布公告（管理员）")
    @PostMapping("/api/admin/notices")
    @RequireRole("ADMIN")
    public Result<String> add(@RequestParam String title, @RequestParam String content) {
        noticeService.add(title, content);
        return Result.success("发布成功");
    }

    /**
     * 编辑公告（管理员）
     */
    @ApiOperation("编辑公告（管理员）")
    @PutMapping("/api/admin/notices/{id}")
    @RequireRole("ADMIN")
    public Result<String> update(@PathVariable Long id,
                                 @RequestParam String title,
                                 @RequestParam String content,
                                 @RequestParam Integer status) {
        noticeService.update(id, title, content, status);
        return Result.success("修改成功");
    }

    /**
     * 删除公告（管理员）
     */
    @ApiOperation("删除公告（管理员）")
    @DeleteMapping("/api/admin/notices/{id}")
    @RequireRole("ADMIN")
    public Result<String> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 所有公告列表（管理员用）
     */
    @ApiOperation("所有公告列表（管理员）")
    @GetMapping("/api/admin/notices")
    @RequireRole("ADMIN")
    public Result<List<SysNotice>> adminList() {
        return Result.success(noticeService.getAllForAdmin());
    }
}