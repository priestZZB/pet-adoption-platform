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
}