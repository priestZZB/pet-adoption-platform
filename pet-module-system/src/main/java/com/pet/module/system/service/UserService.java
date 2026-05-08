package com.pet.module.system.service;

import com.pet.module.system.model.dto.*;
import com.pet.module.system.model.entity.SysUser;
import com.pet.module.system.model.vo.UserInfoVo;
import com.pet.module.system.model.vo.UserListVo;
import java.util.List;

public interface UserService {

    String register(RegisterDto dto);

    String login(LoginDto dto);

    String phoneLogin(PhoneLoginDto dto);

    UserInfoVo getUserInfo(Long userId);

    void updateInfo(Long userId, UserUpdateDto dto);

    void updateAvatar(Long userId, String avatarUrl);

    void changePassword(Long userId, PasswordDto dto);

    void resetPassword(PasswordDto dto);

    void realNameAuth(Long userId, RealNameDto dto);

    List<UserListVo> getUserList(String keyword, int page, int size);

    void toggleUserStatus(Long userId);

    void volunteerApply(Long userId);

    String getVolunteerStatus(Long userId);

    List<SysUser> getVolunteerApplies();

    void donorApply(Long userId);

    String getDonorStatus(Long userId);

    List<SysUser> getDonorApplies();
}