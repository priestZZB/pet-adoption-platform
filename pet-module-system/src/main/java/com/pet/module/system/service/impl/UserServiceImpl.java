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
import com.pet.module.system.service.CaptchaService;
import com.pet.module.system.service.RealNameService;
import com.pet.module.system.service.SmsService;
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

    @Autowired
    private RealNameService realNameService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private CaptchaService captchaService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public void register(RegisterDto dto) {
        if (userMapper.countByUsername(dto.getUsername()) > 0) {
            throw new BusinessException(ResultCodeEnum.USERNAME_EXISTS);
        }

        // 行为验证码校验（防机器注册）
        if (!captchaService.verify(dto.getTicket(), dto.getRandstr(), dto.getCaptchaSign(), null)) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "滑块验证码验证失败，请重试");
        }

        // 短信验证码校验（必传）
        if (dto.getSmsCode() == null || dto.getSmsCode().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "请先获取短信验证码");
        }
        if (!smsService.verifyCode(dto.getPhone(), dto.getSmsCode())) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "短信验证码错误或已过期");
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
        // 行为验证码校验（防暴力破解）
        if (!captchaService.verify(dto.getTicket(), dto.getRandstr(), dto.getCaptchaSign(), null)) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "滑块验证码验证失败，请重试");
        }

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
    public String phoneLogin(PhoneLoginDto dto) {
        // 行为验证码校验（防短信轰炸关联攻击）
        if (!captchaService.verify(dto.getTicket(), dto.getRandstr(), dto.getCaptchaSign(), null)) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "滑块验证码验证失败，请重试");
        }

        if (dto.getPhone() == null || dto.getPhone().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "手机号不能为空");
        }
        if (dto.getSmsCode() == null || dto.getSmsCode().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "请先获取短信验证码");
        }

        // 校验短信验证码
        if (!smsService.verifyCode(dto.getPhone(), dto.getSmsCode())) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "短信验证码错误或已过期");
        }

        // 根据手机号查找用户
        SysUser user = userMapper.selectByPhone(dto.getPhone());
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND, "该手机号未注册");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCodeEnum.USER_DISABLED);
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
        // 行为验证码校验（防密码暴力修改）
        if (!captchaService.verify(dto.getTicket(), dto.getRandstr(), dto.getCaptchaSign(), null)) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "滑块验证码验证失败，请重试");
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        if (!encoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCodeEnum.OLD_PASSWORD_ERROR);
        }

        // 短信验证码校验（修改密码必传）
        if (dto.getPhone() == null || dto.getPhone().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "请提供手机号");
        }
        if (!user.getPhone().equals(dto.getPhone())) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "手机号与当前账号不匹配");
        }
        if (dto.getSmsCode() == null || dto.getSmsCode().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "请先获取短信验证码");
        }
        if (!smsService.verifyCode(dto.getPhone(), dto.getSmsCode())) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "短信验证码错误或已过期");
        }

        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(encoder.encode(dto.getNewPassword()));
        userMapper.updateById(update);
    }

    @Override
    public void resetPassword(PasswordDto dto) {
        // 行为验证码校验（防找回密码滥用）
        if (!captchaService.verify(dto.getTicket(), dto.getRandstr(), dto.getCaptchaSign(), null)) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "滑块验证码验证失败，请重试");
        }

        SysUser user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        if (user.getPhone() == null || !user.getPhone().equals(dto.getPhone())) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID);
        }

        // 短信验证码校验（找回密码必传）
        if (dto.getSmsCode() == null || dto.getSmsCode().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "请先获取短信验证码");
        }
        if (!smsService.verifyCode(dto.getPhone(), dto.getSmsCode())) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "短信验证码错误或已过期");
        }

        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(encoder.encode(dto.getNewPassword()));
        userMapper.updateById(update);
    }

    @Override
    public void realNameAuth(Long userId, RealNameDto dto) {
        // 调用第三方人脸比对（自动识别图片类型）
        boolean verified;
        String image = dto.getImage();
        String imageUrl = dto.getImageUrl();

        // 自动检测：如果 image 字段传了http链接，自动当URL处理
        if (image != null && !image.isEmpty() && image.startsWith("http")) {
            imageUrl = image;
            image = null;
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            verified = realNameService.verifyWithImageUrl(dto.getRealName(), dto.getIdCard(), imageUrl);
        } else if (image != null && !image.isEmpty()) {
            verified = realNameService.verifyWithImage(dto.getRealName(), dto.getIdCard(), image);
        } else {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "请上传人脸图片");
        }
        if (!verified) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "人脸比对未通过，请确认姓名、身份证号与本人一致");
        }

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
        if (user.getIsRealName() != 1) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "请先完成实名认证才能申请志愿者");
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

    @Override
    @Transactional
    public void donorApply(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        if (user.getIsRealName() != 1) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "请先完成实名认证才能申请送养人");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setDonorStatus("PENDING");
        userMapper.updateById(update);
    }

    @Override
    public String getDonorStatus(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        return user.getDonorStatus() != null ? user.getDonorStatus() : "NONE";
    }

    @Override
    public List<SysUser> getDonorApplies() {
        return userMapper.selectDonorApplies();
    }
}