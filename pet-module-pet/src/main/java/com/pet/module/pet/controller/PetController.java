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
     * 宠物详情（公开，登录后可查当前用户收藏状态）
     */
    @ApiOperation("宠物详情")
    @GetMapping("/pets/{id}")
    public Result<PetDetailVo> detail(HttpServletRequest request, @PathVariable Long id) {
        Long userId = null;
        try {
            Object attr = request.getAttribute("userId");
            if (attr != null) {
                userId = Long.valueOf(attr.toString());
            }
        } catch (Exception e) {
            // 未登录时忽略，userId 保持 null
        }
        return Result.success(petService.getPetDetail(id, userId));
    }

    /**
     * 收藏宠物
     */
    @ApiOperation("收藏宠物")
    @PostMapping("/pets/{id}/favorite")
    public Result<String> favorite(HttpServletRequest request, @PathVariable Long id,
                                   @RequestParam(required = false) Long folderId) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petFavoriteService.favorite(userId, id, folderId);
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

    /**
     * 按收藏夹查看收藏
     */
    @ApiOperation("按收藏夹查看收藏")
    @GetMapping("/pets/favorites/by-folder")
    public Result<List<PetListVo>> myFavoritesByFolder(HttpServletRequest request,
                                                         @RequestParam Long folderId) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(petFavoriteService.getMyFavoritesByFolder(userId, folderId));
    }

    /**
     * 移动收藏到指定收藏夹
     */
    @ApiOperation("移动收藏到指定收藏夹")
    @PutMapping("/pets/{id}/favorite/move")
    public Result<String> moveFavorite(HttpServletRequest request, @PathVariable Long id,
                                       @RequestParam(required = false) Long folderId) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petFavoriteService.favorite(userId, id, folderId);
        return Result.success("ok");
    }

    /**
     * 收藏列表+各收藏夹计数（一次性返回）
     */
    @ApiOperation("收藏列表+计数")
    @GetMapping("/pets/favorites/with-counts")
    public Result<java.util.Map<String, Object>> favoritesWithCounts(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(petFavoriteService.getFavoritesWithCounts(userId));
    }

    /**
     * 各收藏夹的收藏数量统计
     */
    @ApiOperation("各收藏夹收藏数量")
    @GetMapping("/pets/favorites/counts")
    public Result<java.util.Map<Long, Integer>> favoriteCounts(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(petFavoriteService.getFavoriteCounts(userId));
    }
}