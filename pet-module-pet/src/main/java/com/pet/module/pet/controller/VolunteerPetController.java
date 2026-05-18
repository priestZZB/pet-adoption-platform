package com.pet.module.pet.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.pet.model.dto.PetReviewDto;
import com.pet.module.pet.model.vo.PetListVo;
import com.pet.module.pet.model.vo.PetSelectVo;
import com.pet.module.pet.model.vo.ReviewHistoryVo;
import com.pet.module.pet.service.PetReviewService;
import com.pet.module.pet.service.PetService;
import com.pet.module.pet.mapper.PetReviewRecordMapper;
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

    @Autowired
    private PetReviewRecordMapper petReviewRecordMapper;

    /**
     * 待审核宠物列表（志愿者，排除自己发布的）
     */
    @ApiOperation("待审核宠物列表")
    @GetMapping("/pets/pending")
    public Result<List<PetListVo>> pending(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(petService.getPendingPets(userId));
    }

    /**
     * 我审核过的宠物列表（去重，每只宠物只显示最新一条审核记录）
     */
    @ApiOperation("我审核过的宠物列表")
    @GetMapping("/pets/reviewed")
    public Result<List<ReviewHistoryVo>> reviewed(HttpServletRequest request) {
        Long reviewerId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(petService.getReviewedByUser(reviewerId));
    }

    /**
     * 获取宠物的所有审核记录（含审核人信息）
     */
    @ApiOperation("宠物审核记录")
    @GetMapping("/pets/{id}/review-records")
    public Result<List<com.pet.module.pet.model.entity.PetReviewRecord>> reviewRecords(@PathVariable Long id) {
        return Result.success(petReviewRecordMapper.selectByPetId(id));
    }

    /**
     * 可选宠物列表（用于走访记录关联）
     */
    @ApiOperation("可选宠物列表（走访记录用）")
    @GetMapping("/pets/selectable")
    public Result<List<PetSelectVo>> selectable() {
        return Result.success(petService.getSelectablePets());
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