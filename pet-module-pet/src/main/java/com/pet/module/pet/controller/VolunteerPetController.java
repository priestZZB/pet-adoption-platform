package com.pet.module.pet.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.pet.model.dto.PetReviewDto;
import com.pet.module.pet.model.vo.PetListVo;
import com.pet.module.pet.service.PetReviewService;
import com.pet.module.pet.service.PetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Log("志愿者审核")
@Api(tags = "志愿者-宠物审核")
@RestController
@RequestMapping("/api/volunteer")
@RequireRole("VOLUNTEER")
public class VolunteerPetController {

    @Autowired
    private PetService petService;

    @Autowired
    private PetReviewService petReviewService;

    /**
     * 待审核宠物列表（志愿者）
     */
    @ApiOperation("待审核宠物列表")
    @GetMapping("/pets/pending")
    public Result<List<PetListVo>> pending() {
        return Result.success(petService.getPendingPets());
    }

    /**
     * 我审核过的宠物列表
     */
    @ApiOperation("我审核过的宠物列表")
    @GetMapping("/pets/reviewed")
    public Result<List<PetListVo>> reviewed(HttpServletRequest request) {
        Long reviewerId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(petService.getReviewedByUser(reviewerId));
    }

    /**
     * 初审（通过/打回）
     */
    @ApiOperation("初审（通过/打回）")
    @PostMapping("/pets/{id}/review")
    public Result<String> review(HttpServletRequest request,
                                 @PathVariable Long id,
                                 @RequestBody PetReviewDto dto) {
        Long reviewerId = Long.valueOf(request.getAttribute("userId").toString());
        petReviewService.firstReview(reviewerId, id, dto);
        return Result.success("审核完成");
    }
}