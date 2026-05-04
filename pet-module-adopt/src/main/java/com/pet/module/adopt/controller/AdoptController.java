package com.pet.module.adopt.controller;

import com.pet.common.result.Result;
import com.pet.module.adopt.model.dto.AdoptApplyDto;
import com.pet.module.adopt.model.vo.AdoptApplyVo;
import com.pet.module.adopt.service.AdoptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/adopt/applications")
public class AdoptController {

    @Autowired
    private AdoptService adoptService;

    /**
     * 提交领养申请（需考试满分）
     */
    @PostMapping
    public Result<String> apply(HttpServletRequest request, @RequestBody AdoptApplyDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        adoptService.apply(userId, dto);
        return Result.success("申请成功，等待审核");
    }

    /**
     * 我的领养申请列表
     */
    @GetMapping
    public Result<List<AdoptApplyVo>> myApplications(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(adoptService.getMyApplications(userId));
    }

    /**
     * 领养申请详情
     */
    @GetMapping("/{id}")
    public Result<AdoptApplyVo> detail(HttpServletRequest request, @PathVariable Long id) {
        return Result.success(adoptService.getApplicationDetail(id));
    }
}