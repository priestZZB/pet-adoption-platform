package com.pet.module.system.controller;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
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
import java.util.Map;
import java.util.stream.Collectors;

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
        Long operatorId = Long.valueOf(request.getAttribute("userId").toString());
        roleService.assignRoles(operatorId, id, roleIds);
        return Result.success("角色修改成功");
    }

    /**
     * 批量启用/禁用用户
     */
    @ApiOperation("批量启用/禁用用户")
    @PostMapping("/users/batch-status")
    public Result<String> batchToggleStatus(HttpServletRequest request,
                                            @RequestBody Map<String, Object> body) {
        Long operatorId = Long.valueOf(request.getAttribute("userId").toString());
        List<Long> ids = extractLongList(body, "ids");
        String action = (String) body.get("action");
        if (action == null || (!"enable".equals(action) && !"disable".equals(action))) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "action 必须是 enable 或 disable");
        }
        userService.batchToggleStatus(operatorId, ids, action);
        return Result.success("批量操作成功");
    }

    /**
     * 批量分配角色
     */
    @ApiOperation("批量分配角色")
    @PostMapping("/users/batch-role")
    public Result<String> batchAssignRole(HttpServletRequest request,
                                          @RequestBody Map<String, Object> body) {
        Long operatorId = Long.valueOf(request.getAttribute("userId").toString());
        List<Long> userIds = extractLongList(body, "userIds");
        List<Long> roleIds = extractLongList(body, "roleIds");
        roleService.batchAssignRoles(operatorId, userIds, roleIds);
        return Result.success("批量分配角色成功");
    }

    /**
     * 从 Map body 中安全提取 List&lt;Long&gt;，支持 Integer/Long/Number 类型
     */
    private List<Long> extractLongList(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "缺少参数: " + key);
        }
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .map(item -> item instanceof Number ? ((Number) item).longValue() : Long.valueOf(item.toString()))
                    .collect(Collectors.toList());
        }
        throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "参数格式错误，应为数组: " + key);
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
    public Result<String> reviewVolunteer(@PathVariable Long id,
                                          @RequestBody VolunteerApplyDto dto) {
        roleService.reviewVolunteerApply(id, dto.getAction(), dto.getRemark());
        return Result.success("审核完成");
    }

    @ApiOperation("送养人申请列表")
    @GetMapping("/donor/applies")
    public Result<List<SysUser>> donorApplies() {
        return Result.success(userService.getDonorApplies());
    }

    @ApiOperation("审核送养人申请")
    @PutMapping("/donor/apply/{id}")
    public Result<String> reviewDonor(@PathVariable Long id,
                                      @RequestBody VolunteerApplyDto dto) {
        roleService.reviewDonorApply(id, dto.getAction(), dto.getRemark());
        return Result.success("审核完成");
    }
}
