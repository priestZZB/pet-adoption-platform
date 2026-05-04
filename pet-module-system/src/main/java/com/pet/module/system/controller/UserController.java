package com.pet.module.system.controller;

import com.pet.common.result.Result;
import com.pet.common.util.FileUtils;
import com.pet.module.system.model.dto.*;
import com.pet.module.system.model.vo.UserInfoVo;
import com.pet.module.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDto dto) {
        userService.register(dto);
        return Result.success("注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDto dto) {
        String token = userService.login(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success("登录成功", data);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("退出成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVo> info(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(userService.getUserInfo(userId));
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/info")
    public Result<String> updateInfo(HttpServletRequest request, @RequestBody UserUpdateDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        userService.updateInfo(userId, dto);
        return Result.success("修改成功");
    }

    /**
     * 上传/修改头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> avatar(HttpServletRequest request,
                                              @RequestParam("file") MultipartFile file) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        String url = FileUtils.upload(file, "avatar");
        userService.updateAvatar(userId, url);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("头像上传成功", data);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<String> changePassword(HttpServletRequest request, @RequestBody PasswordDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        userService.changePassword(userId, dto);
        return Result.success("密码修改成功");
    }

    /**
     * 找回密码
     */
    @PostMapping("/password/reset")
    public Result<String> resetPassword(@RequestBody PasswordDto dto) {
        userService.resetPassword(dto);
        return Result.success("密码重置成功");
    }

    /**
     * 实名认证
     */
    @PostMapping("/real-name")
    public Result<String> realName(HttpServletRequest request, @RequestBody RealNameDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        userService.realNameAuth(userId, dto);
        return Result.success("实名认证成功");
    }

    /**
     * 申请成为志愿者
     */
    @PostMapping("/volunteer/apply")
    public Result<String> volunteerApply(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        userService.volunteerApply(userId);
        return Result.success("申请成功，等待审核");
    }

    /**
     * 查看志愿者申请进度
     */
    @GetMapping("/volunteer/apply/status")
    public Result<Map<String, String>> volunteerStatus(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        String status = userService.getVolunteerStatus(userId);
        Map<String, String> data = new HashMap<>();
        data.put("volunteerStatus", status);
        return Result.success(data);
    }
}