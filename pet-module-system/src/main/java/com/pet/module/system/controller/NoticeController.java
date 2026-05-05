package com.pet.module.system.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.system.model.entity.SysNotice;
import com.pet.module.system.model.vo.NoticeVo;
import com.pet.module.system.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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