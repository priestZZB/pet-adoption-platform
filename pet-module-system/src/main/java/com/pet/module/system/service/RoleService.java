package com.pet.module.system.service;

import com.pet.module.system.model.entity.SysRole;
import java.util.List;

public interface RoleService {

    List<SysRole> getAllRoles();

    SysRole getByCode(String roleCode);

    SysRole getById(Long id);

    List<String> getUserRoleCodes(Long userId);

    void assignRole(Long userId, Long roleId);

    void reviewVolunteerApply(Long userId, String action, String remark);
}