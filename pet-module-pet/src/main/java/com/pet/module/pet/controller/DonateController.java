package com.pet.module.pet.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.pet.model.dto.PetPublishDto;
import com.pet.module.pet.model.dto.PetUpdateDto;
import com.pet.module.pet.model.vo.PetListVo;
import com.pet.module.pet.model.vo.PetTimelineEvent;
import com.pet.module.pet.service.PetService;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.system.model.entity.SysUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Log("送养人管理")
@Api(tags = "送养人-宠物管理")
@RestController
@RequestMapping("/api/donate")
@RequireRole("USER_ADOPTER")
public class DonateController {

    @Autowired
    private PetService petService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 发布送养宠物（含多图上传）
     */
    @ApiOperation("发布送养宠物")
    @PostMapping("/pets")
    public Result<String> publish(HttpServletRequest request, @RequestBody PetPublishDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getIsRealName() != 1) {
            throw new com.pet.common.exception.BusinessException(com.pet.common.enums.ResultCodeEnum.BAD_REQUEST, "请先完成实名认证");
        }
        Long petId = petService.publish(userId, dto);
        return Result.success("发布成功，等待审核");
    }

    /**
     * 编辑宠物信息
     */
    @ApiOperation("编辑宠物信息")
    @PutMapping("/pets/{id}")
    public Result<String> update(HttpServletRequest request,
                                 @PathVariable Long id,
                                 @RequestBody PetUpdateDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petService.update(userId, id, dto);
        return Result.success("修改成功");
    }

    /**
     * 下架宠物
     */
    @ApiOperation("下架宠物")
    @PutMapping("/pets/{id}/offline")
    public Result<String> offline(HttpServletRequest request, @PathVariable Long id) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petService.offline(userId, id);
        return Result.success("已下架");
    }

    /**
     * 撤回送养（仅未被志愿者审核时可撤回）
     */
    @ApiOperation("撤回送养")
    @PutMapping("/pets/{id}/withdraw")
    public Result<String> withdraw(HttpServletRequest request, @PathVariable Long id) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petService.withdraw(userId, id);
        return Result.success("已撤回，重新提交审核");
    }

    /**
     * 我发布的宠物列表
     */
    @ApiOperation("我发布的宠物列表")
    @GetMapping("/pets")
    public Result<List<PetListVo>> myPets(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(petService.getUserPets(userId));
    }

    /**
     * 宠物事件时间线（送养人详情页用）
     */
    @ApiOperation("宠物事件时间线")
    @GetMapping("/pets/{id}/timeline")
    public Result<List<PetTimelineEvent>> timeline(@PathVariable Long id) {
        return Result.success(petService.getPetTimeline(id));
    }
}