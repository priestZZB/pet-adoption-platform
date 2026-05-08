package com.pet.module.pet.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.module.pet.model.vo.PetCategoryVo;
import com.pet.module.pet.model.vo.PetDetailVo;
import com.pet.module.pet.model.vo.PetListVo;
import com.pet.module.pet.service.PetCategoryService;
import com.pet.module.pet.service.PetFavoriteService;
import com.pet.module.pet.service.PetService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Log("宠物浏览")
@Api(tags = "宠物浏览")
@RestController
@RequestMapping("/api")
public class PetController {

    @Autowired
    private PetCategoryService petCategoryService;

    @Autowired
    private PetService petService;

    @Autowired
    private PetFavoriteService petFavoriteService;

    /**
     * 宠物分类列表（公开）
     */
    @ApiOperation("宠物分类列表")
    @GetMapping("/pet/categories")
    public Result<List<PetCategoryVo>> categories() {
        return Result.success(petCategoryService.getCategoryList());
    }

    /**
     * 待领养宠物列表（分页+筛选+搜索，公开）
     */
    @ApiOperation("待领养宠物列表（分页+筛选+搜索）")
    @GetMapping("/pets")
    public Result<PageInfo<PetListVo>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 公开列表默认只展示终审通过的宠物
        String queryStatus = (status != null && !status.isEmpty()) ? status : "APPROVED";
        List<PetListVo> list = petService.getPetList(categoryId, keyword, queryStatus, page, size);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 宠物详情（公开）
     */
    @ApiOperation("宠物详情")
    @GetMapping("/pets/{id}")
    public Result<PetDetailVo> detail(@PathVariable Long id) {
        return Result.success(petService.getPetDetail(id));
    }

    /**
     * 收藏宠物
     */
    @ApiOperation("收藏宠物")
    @PostMapping("/pets/{id}/favorite")
    public Result<String> favorite(HttpServletRequest request, @PathVariable Long id) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petFavoriteService.favorite(userId, id);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏
     */
    @ApiOperation("取消收藏")
    @DeleteMapping("/pets/{id}/favorite")
    public Result<String> unfavorite(HttpServletRequest request, @PathVariable Long id) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petFavoriteService.unfavorite(userId, id);
        return Result.success("已取消收藏");
    }

    /**
     * 我的收藏列表
     */
    @ApiOperation("我的收藏列表")
    @GetMapping("/pets/favorites")
    public Result<List<PetListVo>> myFavorites(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(petFavoriteService.getMyFavorites(userId));
    }
}