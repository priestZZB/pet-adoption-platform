package com.pet.module.system.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.system.model.dto.VolunteerApplyDto;
import com.pet.module.system.model.entity.SysOperationLog;
import com.pet.module.system.model.entity.SysRole;
import com.pet.module.system.model.entity.SysUser;
import com.pet.module.system.model.vo.UserListVo;
import com.pet.module.system.service.OperationLogService;
import com.pet.module.system.service.RoleService;
import com.pet.module.system.service.UserService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Log("系统管理")
@Api(tags = "管理员-系统管理")
@RestController
@RequestMapping("/api/admin")
@RequireRole("ADMIN")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private OperationLogService operationLogService;

    // ===================== 原有接口 =====================

    @ApiOperation("用户列表（分页+搜索）")
    @GetMapping("/users")
    public Result<PageInfo<UserListVo>> userList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        List<UserListVo> list = userService.getUserList(keyword, page, size);
        return Result.success(new PageInfo<>(list));
    }

    @ApiOperation("禁用/启用用户")
    @PutMapping("/user/{id}/status")
    public Result<String> toggleStatus(HttpServletRequest request, @PathVariable Long id) {
        Long operatorId = Long.valueOf(request.getAttribute("userId").toString());
        userService.toggleUserStatus(operatorId, id);
        operationLogService.addLog(
                operatorId, null,
                "用户管理", "切换用户状态: id=" + id,
                request.getRemoteAddr()
        );
        return Result.success("操作成功");
    }

    @ApiOperation("角色列表")
    @GetMapping("/roles")
    public Result<List<SysRole>> roles() {
        return Result.success(roleService.getAllRoles());
    }

    @ApiOperation("修改用户角色")
    @PutMapping("/user/{id}/role")
    public Result<String> assignRole(HttpServletRequest request,
                                     @PathVariable Long id,
                                     @RequestParam List<Long> roleIds) {
        roleService.assignRoles(id, roleIds);
        operationLogService.addLog(
                Long.valueOf(request.getAttribute("userId").toString()), null,
                "用户管理", "修改用户角色: userId=" + id + ", roleIds=" + roleIds,
                request.getRemoteAddr()
        );
        return Result.success("角色修改成功");
    }

    @ApiOperation("操作日志列表")
    @GetMapping("/logs")
    public Result<PageInfo<SysOperationLog>> logs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String module) {
        List<SysOperationLog> list = operationLogService.getLogList(module, page, size);
        return Result.success(new PageInfo<>(list));
    }

    @ApiOperation("志愿者申请列表")
    @GetMapping("/volunteer/applies")
    public Result<List<SysUser>> volunteerApplies() {
        return Result.success(userService.getVolunteerApplies());
    }

    @ApiOperation("审核志愿者申请")
    @PutMapping("/volunteer/apply/{id}")
    public Result<String> reviewVolunteer(HttpServletRequest request,
                                          @PathVariable Long id,
                                          @RequestBody VolunteerApplyDto dto) {
        Long adminId = Long.valueOf(request.getAttribute("userId").toString());
        roleService.reviewVolunteerApply(id, dto.getAction(), dto.getRemark());
        operationLogService.addLog(adminId, null,
                "志愿者管理", "审核志愿者申请: userId=" + id + ", action=" + dto.getAction(),
                request.getRemoteAddr());
        return Result.success("审核完成");
    }

    @ApiOperation("送养人申请列表")
    @GetMapping("/donor/applies")
    public Result<List<SysUser>> donorApplies() {
        return Result.success(userService.getDonorApplies());
    }

    @ApiOperation("审核送养人申请")
    @PutMapping("/donor/apply/{id}")
    public Result<String> reviewDonor(HttpServletRequest request,
                                      @PathVariable Long id,
                                      @RequestBody VolunteerApplyDto dto) {
        Long adminId = Long.valueOf(request.getAttribute("userId").toString());
        roleService.reviewDonorApply(id, dto.getAction(), dto.getRemark());
        operationLogService.addLog(adminId, null,
                "送养人管理", "审核送养人申请: userId=" + id + ", action=" + dto.getAction(),
                request.getRemoteAddr());
        return Result.success("审核完成");
    }
}
