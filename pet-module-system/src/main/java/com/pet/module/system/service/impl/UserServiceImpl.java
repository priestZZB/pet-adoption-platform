package com.pet.module.system.service.impl;

import cn.hutool.core.util.RandomUtil;
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
    public String register(RegisterDto dto) {
        // 检查手机号是否已注册
        if (userMapper.countByPhone(dto.getPhone()) > 0) {
            throw new BusinessException(ResultCodeEnum.PHONE_EXISTS);
        }

        // 发短信时已验证过滑块，此处不再重复验证
        // 短信验证码校验（必传）
        if (dto.getSmsCode() == null || dto.getSmsCode().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "请先获取短信验证码");
        }
        if (!smsService.verifyCode(dto.getPhone(), dto.getSmsCode())) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "短信验证码错误或已过期");
        }

        // 使用前端传的用户名（已预生成），若被占用则重新生成
        String username = dto.getUsername();
        if (username == null || username.isEmpty() || userMapper.countByUsername(username) > 0) {
            do {
                username = "24" + RandomUtil.randomNumbers(7);
            } while (userMapper.countByUsername(username) > 0);
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null && !dto.getNickname().isEmpty() ? dto.getNickname() : username);
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

        return username;
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
        // 发短信时已验证过滑块，此处不再重复验证
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
    public String generateUsername() {
        String username;
        do {
            username = "24" + RandomUtil.randomNumbers(7);
        } while (userMapper.countByUsername(username) > 0);
        return username;
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
        // 发短信时已验证过滑块，此处不再重复验证
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        if (!encoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCodeEnum.OLD_PASSWORD_ERROR);
        }

        // 检查新旧密码是否相同
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "新密码不能与旧密码相同");
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
        // 发短信时已验证过滑块，此处不再重复验证
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
    @Transactional
    public void realNameAuth(Long userId, RealNameDto dto) {
        // 行为验证码校验（防恶意批量实名认证）
        if (!captchaService.verify(dto.getTicket(), dto.getRandstr(), dto.getCaptchaSign(), null)) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "滑块验证码验证失败，请重试");
        }

        // 检查身份证号是否已被其他账号绑定
        SysUser existingUser = userMapper.selectByIdCard(dto.getIdCard());
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            // 对已绑定账号的用户名做加密处理（只显示前2位+***+后2位）
            String boundUsername = maskUsername(existingUser.getUsername());
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST,
                    "该身份证已被账号「" + boundUsername + "」绑定");
        }

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

    /**
     * 对用户名做脱敏处理：显示前2位+***+后2位
     */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 4) {
            return username;
        }
        return username.substring(0, 2) + "***" + username.substring(username.length() - 2);
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