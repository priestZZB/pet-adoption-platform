package com.pet.module.system.service;

import com.pet.module.system.model.dto.*;
import com.pet.module.system.model.entity.SysUser;
import com.pet.module.system.model.vo.UserInfoVo;
import com.pet.module.system.model.vo.UserListVo;
import java.util.List;

public interface UserService {

    String register(RegisterDto dto);

    String generateUsername();

    String login(LoginDto dto);

    String phoneLogin(PhoneLoginDto dto);

    UserInfoVo getUserInfo(Long userId);

    void updateInfo(Long userId, UserUpdateDto dto);

    void updateAvatar(Long userId, String avatarUrl);

    void changePassword(Long userId, PasswordDto dto);

    void resetPassword(PasswordDto dto);

    void realNameAuth(Long userId, RealNameDto dto);

    /**
     * 实名认证（免滑块验证，用于活体检测后直接调用的流程）
     */
    void realNameAuthDirect(Long userId, RealNameDto dto);

    /**
     * 检查身份证号是否可绑定（未被其他账号使用）
     * @throws BusinessException 如果已被绑定
     */
    void checkIdCardAvailable(Long userId, String idCard);

    List<UserListVo> getUserList(String keyword, int page, int size);

    void toggleUserStatus(Long operatorId, Long targetUserId);

    void volunteerApply(Long userId);

    String getVolunteerStatus(Long userId);

    List<SysUser> getVolunteerApplies();

    void donorApply(Long userId);

    String getDonorStatus(Long userId);

    List<SysUser> getDonorApplies();
}