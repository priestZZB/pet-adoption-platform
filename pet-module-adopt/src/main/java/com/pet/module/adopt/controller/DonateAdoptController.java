package com.pet.module.adopt.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.adopt.model.vo.AdoptApplyVo;
import com.pet.module.adopt.service.AdoptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log("领养审核")
@Api(tags = "送养人-领养审核")
@RestController
@RequestMapping("/api/donate")
@RequireRole("USER_ADOPTER")
public class DonateAdoptController {

    @Autowired
    private AdoptService adoptService;

    /**
     * 查看领养申请列表（送养人）
     */
    @ApiOperation("查看领养申请列表（送养人）")
    @GetMapping("/pets/{id}/applications")
    public Result<List<AdoptApplyVo>> petApplications(@PathVariable Long id) {
        return Result.success(adoptService.getPetApplications(id));
    }

    /**
     * 同意/拒绝领养申请（送养人）
     */
    @ApiOperation("同意/拒绝领养申请（送养人）")
    @PutMapping("/application/{id}")
    public Result<String> review(@PathVariable Long id, @RequestParam String action) {
        adoptService.donorReview(id, action);
        return Result.success("操作成功");
    }
}
