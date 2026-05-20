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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        // 单角色兼容：保持原行为
        assignRoles(userId, java.util.Collections.singletonList(roleId));
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
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
        // 选中了送养人角色 → APPROVED，没选中 → NONE（重置申请状态）
        update.setDonorStatus(hasDonor ? "APPROVED" : "NONE");
        // 选中了志愿者角色 → APPROVED，没选中 → NONE（重置申请状态）
        update.setVolunteerStatus(hasVolunteer ? "APPROVED" : "NONE");
        userMapper.updateById(update);
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