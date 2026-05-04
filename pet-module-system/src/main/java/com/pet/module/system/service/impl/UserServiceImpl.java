package com.pet.module.system.service.impl;

import com.github.pagehelper.PageHelper;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.framework.util.JwtUtils;
import com.pet.module.system.mapper.RoleMapper;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.system.mapper.UserRoleMapper;
import com.pet.module.system.model.dto.*;
import com.pet.module.system.model.entity.SysRole;
import com.pet.module.system.model.entity.SysUser;
import com.pet.module.system.model.entity.SysUserRole;
import com.pet.module.system.model.vo.UserInfoVo;
import com.pet.module.system.model.vo.UserListVo;
import com.pet.module.system.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public void register(RegisterDto dto) {
        if (userMapper.countByUsername(dto.getUsername()) > 0) {
            throw new BusinessException(ResultCodeEnum.USERNAME_EXISTS);
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        userMapper.insert(user);

        // 分配默认角色 USER
        SysRole role = roleMapper.selectByCode("USER");
        if (role != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(role.getId());
            userRoleMapper.insert(ur);
        }
    }

    @Override
    public String login(LoginDto dto) {
        SysUser user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCodeEnum.USER_DISABLED);
        }
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCodeEnum.PASSWORD_INCORRECT);
        }

        List<String> roles = userRoleMapper.selectRoleCodesByUserId(user.getId());
        String role = roles.isEmpty() ? "USER" : roles.get(0);

        return jwtUtils.generateToken(String.valueOf(user.getId()), role);
    }

    @Override
    public UserInfoVo getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }

        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(user, vo);
        vo.setRoles(userRoleMapper.selectRoleCodesByUserId(userId));
        return vo;
    }

    @Override
    public void updateInfo(Long userId, UserUpdateDto dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }

        SysUser update = new SysUser();
        update.setId(userId);
        update.setNickname(dto.getNickname());
        update.setEmail(dto.getEmail());
        update.setPhone(dto.getPhone());
        userMapper.updateById(update);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        SysUser update = new SysUser();
        update.setId(userId);
        update.setAvatar(avatarUrl);
        userMapper.updateById(update);
    }

    @Override
    public void changePassword(Long userId, PasswordDto dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        if (!encoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCodeEnum.OLD_PASSWORD_ERROR);
        }

        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(encoder.encode(dto.getNewPassword()));
        userMapper.updateById(update);
    }

    @Override
    public void resetPassword(PasswordDto dto) {
        SysUser user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        if (user.getPhone() == null || !user.getPhone().equals(dto.getPhone())) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID);
        }

        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(encoder.encode(dto.getNewPassword()));
        userMapper.updateById(update);
    }

    @Override
    public void realNameAuth(Long userId, RealNameDto dto) {
        SysUser update = new SysUser();
        update.setId(userId);
        update.setRealName(dto.getRealName());
        update.setIdCard(dto.getIdCard());
        update.setPhone(dto.getPhone());
        update.setIsRealName(1);
        userMapper.updateById(update);
    }

    @Override
    public List<UserListVo> getUserList(String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<SysUser> userList = userMapper.selectPage(keyword);

        return userList.stream().map(user -> {
            UserListVo vo = new UserListVo();
            BeanUtils.copyProperties(user, vo);
            vo.setRoles(userRoleMapper.selectRoleCodesByUserId(user.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void toggleUserStatus(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setStatus(user.getStatus() == 0 ? 1 : 0);
        userMapper.updateById(update);
    }

    @Override
    @Transactional
    public void volunteerApply(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setVolunteerStatus("PENDING");
        userMapper.updateById(update);
    }

    @Override
    public String getVolunteerStatus(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        return user.getVolunteerStatus() != null ? user.getVolunteerStatus() : "NONE";
    }

    @Override
    public List<SysUser> getVolunteerApplies() {
        return userMapper.selectVolunteerApplies();
    }
}