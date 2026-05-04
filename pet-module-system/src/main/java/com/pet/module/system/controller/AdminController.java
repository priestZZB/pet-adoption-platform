package com.pet.module.system.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.system.model.entity.SysOperationLog;
import com.pet.module.system.model.entity.SysRole;
import com.pet.module.system.model.vo.UserListVo;
import com.pet.module.system.service.OperationLogService;
import com.pet.module.system.service.RoleService;
import com.pet.module.system.service.UserService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 用户列表（分页 + 关键词搜索）
     */
    @GetMapping("/users")
    public Result<PageInfo<UserListVo>> userList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        List<UserListVo> list = userService.getUserList(keyword, page, size);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/user/{id}/status")
    public Result<String> toggleStatus(HttpServletRequest request, @PathVariable Long id) {
        userService.toggleUserStatus(id);
        String userId = request.getAttribute("userId").toString();
        operationLogService.addLog(
                Long.valueOf(userId), null,
                "用户管理", "切换用户状态: id=" + id,
                request.getRemoteAddr()
        );
        return Result.success("操作成功");
    }

    /**
     * 角色列表
     */
    @GetMapping("/roles")
    public Result<List<SysRole>> roles() {
        return Result.success(roleService.getAllRoles());
    }

    /**
     * 修改用户角色
     */
    @PutMapping("/user/{id}/role")
    public Result<String> assignRole(HttpServletRequest request,
                                     @PathVariable Long id,
                                     @RequestParam Long roleId) {
        roleService.assignRole(id, roleId);
        operationLogService.addLog(
                Long.valueOf(request.getAttribute("userId").toString()), null,
                "用户管理", "修改用户角色: userId=" + id + ", roleId=" + roleId,
                request.getRemoteAddr()
        );
        return Result.success("角色修改成功");
    }

    /**
     * 操作日志列表
     */
    @GetMapping("/logs")
    public Result<PageInfo<SysOperationLog>> logs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String module) {
        List<SysOperationLog> list = operationLogService.getLogList(module, page, size);
        return Result.success(new PageInfo<>(list));
    }
}