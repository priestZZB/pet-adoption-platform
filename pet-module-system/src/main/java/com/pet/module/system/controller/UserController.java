package com.pet.module.system.controller;

import com.pet.common.result.Result;
import com.pet.common.util.FileUtils;
import com.pet.framework.annotation.Log;
import com.pet.framework.util.JwtUtils;
import com.pet.module.system.model.dto.*;
import com.pet.module.system.model.vo.UserInfoVo;
import com.pet.module.system.service.RealNameService;
import com.pet.module.system.service.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Log("用户管理")
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RealNameService realNameService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 预生成用户名（注册页面加载时调用）
     */
    @ApiOperation("预生成用户名")
    @GetMapping("/generate-username")
    public Result<Map<String, String>> generateUsername() {
        String username = userService.generateUsername();
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        return Result.success(data);
    }

    /**
     * 用户注册
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Map<String, String>> register(@RequestBody RegisterDto dto) {
        String username = userService.register(dto);
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        return Result.success("注册成功", data);
    }

    /**
     * 用户登录（用户名+密码）
     */
    @ApiOperation("用户登录（用户名+密码）")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDto dto) {
        String token = userService.login(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success("登录成功", data);
    }

    /**
     * 手机号登录（手机号+短信验证码）
     */
    @ApiOperation("手机号+短信验证码登录")
    @PostMapping("/login/phone")
    public Result<Map<String, Object>> phoneLogin(@RequestBody PhoneLoginDto dto) {
        String token = userService.phoneLogin(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success("登录成功", data);
    }

    /**
     * 退出登录 — 将 Token 加入 Redis 黑名单，使其立即失效
     */
    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtils.parseToken(token);
                String jti = claims.getId();
                long ttl = jwtUtils.getRemainingTtl(claims);
                if (ttl > 0 && jti != null) {
                    // Token 的剩余有效期作为黑名单的 TTL
                    redisTemplate.opsForValue().set(
                            "blacklist:token:" + jti,
                            "1",
                            ttl,
                            TimeUnit.MILLISECONDS
                    );
                }
            } catch (Exception e) {
                // token 已过期等情况忽略，前端清除 localStorage 即可
            }
        }
        return Result.success("退出成功");
    }

    /**
     * 获取当前用户信息
     */
    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<UserInfoVo> info(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(userService.getUserInfo(userId));
    }

    /**
     * 修改个人信息
     */
    @ApiOperation("修改个人信息")
    @PutMapping("/info")
    public Result<String> updateInfo(HttpServletRequest request, @RequestBody UserUpdateDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        userService.updateInfo(userId, dto);
        return Result.success("修改成功");
    }

    /**
     * 上传/修改头像
     */
    @ApiOperation("上传/修改头像")
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
    @ApiOperation("修改密码")
    @PutMapping("/password")
    public Result<String> changePassword(HttpServletRequest request, @RequestBody PasswordDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        userService.changePassword(userId, dto);
        return Result.success("密码修改成功");
    }

    /**
     * 找回密码
     */
    @ApiOperation("找回密码")
    @PostMapping("/password/reset")
    public Result<String> resetPassword(@RequestBody PasswordDto dto) {
        userService.resetPassword(dto);
        return Result.success("密码重置成功");
    }

    /**
     * 实名认证
     */
    /**
     * 检查身份证号是否可绑定
     */
    @ApiOperation("检查身份证号是否可绑定")
    @GetMapping("/idcard/check")
    public Result<String> checkIdCard(HttpServletRequest request, @RequestParam String idCard) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        userService.checkIdCardAvailable(userId, idCard);
        return Result.success("身份证号可用");
    }

    @ApiOperation("实名认证")
    @PostMapping("/real-name")
    public Result<String> realName(HttpServletRequest request, @RequestBody RealNameDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        userService.realNameAuth(userId, dto);
        return Result.success("实名认证成功");
    }

    /**
     * 实名认证（免滑块验证，活体检测后直接调）
     */
    @ApiOperation("实名认证（免滑块）")
    @PostMapping("/real-name/direct")
    public Result<String> realNameDirect(HttpServletRequest request, @RequestBody RealNameDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        userService.realNameAuthDirect(userId, dto);
        return Result.success("实名认证成功");
    }

    /**
     * 获取活体检测H5页面
     */
    @ApiOperation("获取活体检测H5页面")
    @PostMapping("/liveness/token")
    public Result<Map<String, String>> livenessToken(@RequestBody Map<String, String> params) {
        String returnUrl = params.get("returnUrl");
        if (returnUrl == null || returnUrl.isEmpty()) {
            return Result.error(400, "returnUrl不能为空");
        }
        Map<String, String> result = realNameService.getLivenessToken(returnUrl);
        if (result.containsKey("error")) {
            return Result.error(500, result.get("error"));
        }
        return Result.success(result);
    }

    /**
     * 查询活体检测结果
     */
    @ApiOperation("查询活体检测结果")
    @PostMapping("/liveness/result")
    public Result<Map<String, Object>> livenessResult(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        if (orderNo == null || orderNo.isEmpty()) {
            return Result.error(400, "orderNo不能为空");
        }
        String hbId = params.getOrDefault("hbId", "");
        Map<String, Object> result = realNameService.checkLivenessResult(orderNo, hbId);
        return Result.success(result);
    }

    /**
     * 申请成为志愿者
     */
    @ApiOperation("申请成为志愿者")
    @PostMapping("/volunteer/apply")
    public Result<String> volunteerApply(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        userService.volunteerApply(userId);
        return Result.success("申请成功，等待审核");
    }

    /**
     * 查看志愿者申请进度
     */
    @ApiOperation("查看志愿者申请进度")
    @GetMapping("/volunteer/apply/status")
    public Result<Map<String, String>> volunteerStatus(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        String status = userService.getVolunteerStatus(userId);
        Map<String, String> data = new HashMap<>();
        data.put("volunteerStatus", status);
        return Result.success(data);
    }

    /**
     * 申请成为送养人
     */
    @ApiOperation("申请成为送养人")
    @PostMapping("/donor/apply")
    public Result<String> donorApply(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        userService.donorApply(userId);
        return Result.success("申请成功，等待审核");
    }

    /**
     * 查看送养人申请进度
     */
    @ApiOperation("查看送养人申请进度")
    @GetMapping("/donor/apply/status")
    public Result<Map<String, String>> donorStatus(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        String status = userService.getDonorStatus(userId);
        Map<String, String> data = new HashMap<>();
        data.put("donorStatus", status);
        return Result.success(data);
    }
}