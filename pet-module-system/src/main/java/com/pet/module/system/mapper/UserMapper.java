package com.pet.module.system.mapper;

import com.pet.module.system.model.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    int insert(SysUser user);

    SysUser selectById(Long id);

    SysUser selectByUsername(String username);

    int updateById(SysUser user);

    List<SysUser> selectPage(@Param("keyword") String keyword);

    int countByUsername(String username);

    int countByPhone(String phone);

    int updateVolunteerStatus(@Param("userId") Long userId, @Param("volunteerStatus") String volunteerStatus);

    List<SysUser> selectVolunteerApplies();

    List<SysUser> selectDonorApplies();
}