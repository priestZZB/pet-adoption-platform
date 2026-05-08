package com.pet.module.system.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.system.mapper.RoleMapper;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.system.mapper.UserRoleMapper;
import com.pet.module.system.model.entity.SysRole;
import com.pet.module.system.model.entity.SysUser;
import com.pet.module.system.model.entity.SysUserRole;
import com.pet.module.system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
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
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "角色不存在");
        }
        userRoleMapper.deleteByUserId(userId);
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        userRoleMapper.insert(ur);

        // 同步更新状态字段，保持两套体系一致
        SysUser update = new SysUser();
        update.setId(userId);
        if ("USER_ADOPTER".equals(role.getRoleCode())) {
            update.setDonorStatus("APPROVED");
        } else if ("VOLUNTEER".equals(role.getRoleCode())) {
            update.setVolunteerStatus("APPROVED");
        } else if ("USER".equals(role.getRoleCode())) {
            // 改为普通用户时，重置申请状态（撤销已通过的身份申请）
            update.setDonorStatus("NONE");
            update.setVolunteerStatus("NONE");
        }
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
        } else if ("REJECTED".equals(action)) {
            update.setDonorStatus("REJECTED");
            userMapper.updateById(update);
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
        } else if ("REJECTED".equals(action)) {
            update.setVolunteerStatus("REJECTED");
            userMapper.updateById(update);
        }
    }
}