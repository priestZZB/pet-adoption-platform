package com.pet.module.volunteer.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.volunteer.model.dto.VisitDto;
import com.pet.module.volunteer.model.vo.VisitVo;
import com.pet.module.volunteer.service.VisitService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/volunteer")
@RequireRole("VOLUNTEER")
public class VolunteerController {

    @Autowired
    private VisitService visitService;

    /**
     * 提交走访记录（含多图）
     */
    @PostMapping("/visits")
    public Result<String> addVisit(HttpServletRequest request, @RequestBody VisitDto dto) {
        Long volunteerId = Long.valueOf(request.getAttribute("userId").toString());
        visitService.add(volunteerId, dto);
        return Result.success("提交成功");
    }

    /**
     * 我的走访记录列表（分页）
     */
    @GetMapping("/visits")
    public Result<PageInfo<VisitVo>> myVisits(HttpServletRequest request,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        Long volunteerId = Long.valueOf(request.getAttribute("userId").toString());
        List<VisitVo> list = visitService.getMyVisits(volunteerId, page, size);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 走访记录详情
     */
    @GetMapping("/visits/{id}")
    public Result<VisitVo> visitDetail(@PathVariable Long id) {
        return Result.success(visitService.getVisitDetail(id));
    }
}