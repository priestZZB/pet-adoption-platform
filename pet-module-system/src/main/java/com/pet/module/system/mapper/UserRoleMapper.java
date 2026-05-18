package com.pet.module.system.mapper;

import com.pet.module.system.model.entity.SysUserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserRoleMapper {

    int insert(SysUserRole userRole);

    List<SysUserRole> selectByUserId(Long userId);

    int deleteByUserId(Long userId);

    List<String> selectRoleCodesByUserId(Long userId);

    List<Long> selectRoleIdsByUserId(Long userId);

    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 根据角色编码查询所有用户ID
     * 用于批量通知场景（如通知所有管理员/志愿者）
     */
    List<Long> selectUserIdsByRoleCode(@Param("roleCode") String roleCode);
}