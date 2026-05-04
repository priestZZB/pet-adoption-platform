package com.pet.module.system.mapper;

import com.pet.module.system.model.entity.SysRole;

import java.util.List;

public interface RoleMapper {

    List<SysRole> selectAll();

    SysRole selectByCode(String roleCode);

    SysRole selectById(Long id);
}