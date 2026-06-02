package com.pet.module.system.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.event.NotificationEvent;
import com.pet.common.exception.BusinessException;
import com.pet.module.system.mapper.RoleMapper;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.system.mapper.UserRoleMapper;
import com.pet.module.system.model.entity.SysRole;
import com.pet.module.system.model.entity.SysUser;
import com.pet.module.system.model.entity.SysUserRole;
import com.pet.module.system.service.RoleService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    private final UserRoleMapper userRoleMapper;

    private final UserMapper userMapper;

    private final ApplicationEventPublisher eventPublisher;

    private final JdbcTemplate jdbcTemplate;

    public RoleServiceImpl(
            RoleMapper roleMapper,
            UserRoleMapper userRoleMapper,
            UserMapper userMapper,
            ApplicationEventPublisher eventPublisher,
            JdbcTemplate jdbcTemplate) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public List<SysRole> getAllRoles() {
        return roleMapper.selectAll();
    }

    @Override
    public SysRole getByCode(String roleCode) {
        return roleMapper.selectByCode(roleCode);
    }

    @Override
    public SysRole getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public List<String> getUserRoleCodes(Long userId) {
        return userRoleMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    @Transactional
    public void assignRole(Long userId, Long roleId) {
        // 单角色兼容：保持原行为（未知操作人时不做权限校验）
        assignRoles(null, userId, java.util.Collections.singletonList(roleId));
    }

    @Override
    @Transactional
    public void assignRoles(Long operatorId, Long userId, List<Long> roleIds) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }

        // ====== 超级管理员权限校验 ======
        if (operatorId != null) {
            SysUser operator = userMapper.selectById(operatorId);
            boolean opIsSuper = operator != null && Integer.valueOf(1).equals(operator.getIsSuperAdmin());
            boolean targetIsAdmin = userRoleMapper.selectRoleCodesByUserId(userId)
                    .contains("ADMIN");
            SysRole adminRole = roleMapper.selectByCode("ADMIN");

            // 规则1：管理员不能移除自己的ADMIN角色
            if (operatorId.equals(userId) && adminRole != null && !roleIds.contains(adminRole.getId())) {
                throw new BusinessException(ResultCodeEnum.BAD_REQUEST,
                        "不能移除自己的管理员身份");
            }

            // 规则2：非超级管理员不能修改其他管理员的角色
            if (targetIsAdmin && !opIsSuper && !operatorId.equals(userId)) {
                throw new BusinessException(ResultCodeEnum.ROLE_REQUIRED,
                        "只有超级管理员才能修改管理员角色");
            }

            // 规则3：非超级管理员不能把普通用户提升为管理员
            if (!opIsSuper && adminRole != null && roleIds.contains(adminRole.getId()) && !targetIsAdmin) {
                throw new BusinessException(ResultCodeEnum.ROLE_REQUIRED,
                        "只有超级管理员才能任命管理员");
            }
        }

        // 获取当前用户已有角色
        List<Long> currentRoleIds = userRoleMapper.selectRoleIdsByUserId(userId);

        // 计算需要删除的角色（已有但不在新列表中的）
        for (Long rid : currentRoleIds) {
            if (!roleIds.contains(rid)) {
                userRoleMapper.deleteByUserIdAndRoleId(userId, rid);
            }
        }

        // 计算需要新增的角色（新列表中有但当前没有的）
        for (Long rid : roleIds) {
            if (!currentRoleIds.contains(rid)) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(rid);
                userRoleMapper.insert(ur);
            }
        }

        // 同步更新状态字段：根据选中的角色设置对应审批状态
        boolean hasDonor = false, hasVolunteer = false;
        for (Long rid : roleIds) {
            SysRole r = roleMapper.selectById(rid);
            if (r == null) continue;
            if ("USER_ADOPTER".equals(r.getRoleCode())) hasDonor = true;
            if ("VOLUNTEER".equals(r.getRoleCode())) hasVolunteer = true;
        }

        // 查询当前用户状态
        SysUser cur = userMapper.selectById(userId);

        // 如果取消了送养人角色，同步下架其发布的宠物 + 发通知
        if (!hasDonor && cur != null && "APPROVED".equals(cur.getDonorStatus())) {
            jdbcTemplate.update("UPDATE pet_info SET status = 'OFFLINE' WHERE user_id = ?", userId);
            eventPublisher.publishEvent(new NotificationEvent(
                    userId, "DONOR_REMOVED",
                    "送养人身份已取消",
                    "管理员已取消你的送养人身份，你发布的宠物已下架",
                    userId));
        }

        // 如果取消了志愿者角色，发通知
        if (!hasVolunteer && cur != null && "APPROVED".equals(cur.getVolunteerStatus())) {
            eventPublisher.publishEvent(new NotificationEvent(
                    userId, "VOLUNTEER_REMOVED",
                    "志愿者身份已取消",
                    "管理员已取消你的志愿者身份",
                    userId));
        }

        // 如果新增了送养人角色，发通知
        if (hasDonor && cur != null && !"APPROVED".equals(cur.getDonorStatus())) {
            eventPublisher.publishEvent(new NotificationEvent(
                    userId, "DONOR_ADDED",
                    "送养人身份已开通",
                    "管理员已为你开通送养人身份，现在可以发布送养宠物了",
                    userId));
        }

        // 如果新增了志愿者角色，发通知
        if (hasVolunteer && cur != null && !"APPROVED".equals(cur.getVolunteerStatus())) {
            eventPublisher.publishEvent(new NotificationEvent(
                    userId, "VOLUNTEER_ADDED",
                    "志愿者身份已开通",
                    "管理员已为你开通志愿者身份，现在可以参与审核和走访了",
                    userId));
        }

        SysUser update = new SysUser();
        update.setId(userId);
        boolean needUpdate = false;

        // 仅在实际变更时更新状态：授予→APPROVED，取消已 APPROVED→NONE
        // 保留 PENDING / REJECTED 等中间状态不被覆盖
        if (hasDonor && cur != null && !"APPROVED".equals(cur.getDonorStatus())) {
            update.setDonorStatus("APPROVED");
            needUpdate = true;
        } else if (!hasDonor && cur != null && "APPROVED".equals(cur.getDonorStatus())) {
            update.setDonorStatus("NONE");
            needUpdate = true;
        }

        if (hasVolunteer && cur != null && !"APPROVED".equals(cur.getVolunteerStatus())) {
            update.setVolunteerStatus("APPROVED");
            needUpdate = true;
        } else if (!hasVolunteer && cur != null && "APPROVED".equals(cur.getVolunteerStatus())) {
            update.setVolunteerStatus("NONE");
            needUpdate = true;
        }

        if (needUpdate) {
            userMapper.updateById(update);
        }
    }

    @Override
    @Transactional
    public void reviewDonorApply(Long userId, String action, String remark) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        SysUser update = new SysUser();
        update.setId(userId);
        if ("APPROVED".equals(action)) {
            update.setDonorStatus("APPROVED");
            userMapper.updateById(update);

            SysRole role = roleMapper.selectByCode("USER_ADOPTER");
            if (role != null) {
                List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
                boolean hasRole = roleIds.contains(role.getId());
                if (!hasRole) {
                    SysUserRole ur = new SysUserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(role.getId());
                    userRoleMapper.insert(ur);
                }
            }

            eventPublisher.publishEvent(new NotificationEvent(
                    userId, "DONOR_REVIEW",
                    "送养人申请已通过",
                    "你的送养人申请已通过审核，现在可以发布送养宠物了",
                    userId));
        } else if ("REJECTED".equals(action)) {
            update.setDonorStatus("REJECTED");
            userMapper.updateById(update);

            String reason = (remark != null && !remark.isEmpty()) ? remark : "不符合条件";
            eventPublisher.publishEvent(new NotificationEvent(
                    userId, "DONOR_REVIEW",
                    "送养人申请未通过",
                    "你的送养人申请未通过审核，原因：" + reason,
                    userId));
        }
    }

    @Override
    @Transactional
    public void batchAssignRoles(Long operatorId, List<Long> userIds, List<Long> roleIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "请选择至少一个用户");
        }
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "请选择至少一个角色");
        }
        for (Long userId : userIds) {
            assignRoles(operatorId, userId, roleIds);
        }
    }

    @Override
    @Transactional
    public void reviewVolunteerApply(Long userId, String action, String remark) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        SysUser update = new SysUser();
        update.setId(userId);
        if ("APPROVED".equals(action)) {
            update.setVolunteerStatus("APPROVED");
            userMapper.updateById(update);

            SysRole role = roleMapper.selectByCode("VOLUNTEER");
            if (role != null) {
                List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
                boolean hasVolunteer = roleIds.contains(role.getId());
                if (!hasVolunteer) {
                    SysUserRole ur = new SysUserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(role.getId());
                    userRoleMapper.insert(ur);
                }
            }

            eventPublisher.publishEvent(new NotificationEvent(
                    userId, "VOLUNTEER_REVIEW",
                    "志愿者申请已通过",
                    "你的志愿者申请已通过审核，现在可以参与宠物审核和走访记录了",
                    userId));
        } else if ("REJECTED".equals(action)) {
            update.setVolunteerStatus("REJECTED");
            userMapper.updateById(update);

            String reason = (remark != null && !remark.isEmpty()) ? remark : "不符合条件";
            eventPublisher.publishEvent(new NotificationEvent(
                    userId, "VOLUNTEER_REVIEW",
                    "志愿者申请未通过",
                    "你的志愿者申请未通过审核，原因：" + reason,
                    userId));
        }
    }
}