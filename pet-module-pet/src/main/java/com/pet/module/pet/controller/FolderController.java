package com.pet.module.pet.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.module.pet.model.entity.PetFavoriteFolder;
import com.pet.module.pet.service.FolderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Log("收藏夹")
@Api(tags = "收藏夹")
@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @ApiOperation("创建收藏夹")
    @PostMapping
    public Result<Long> create(HttpServletRequest request, @RequestParam String name) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(folderService.create(userId, name));
    }

    @ApiOperation("重命名收藏夹")
    @PutMapping("/{id}")
    public Result<String> rename(HttpServletRequest request, @PathVariable Long id, @RequestParam String name) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        folderService.rename(userId, id, name);
        return Result.success("重命名成功");
    }

    @ApiOperation("删除收藏夹")
    @DeleteMapping("/{id}")
    public Result<String> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        folderService.delete(userId, id);
        return Result.success("已删除");
    }

    @ApiOperation("我的收藏夹列表")
    @GetMapping
    public Result<List<PetFavoriteFolder>> myFolders(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(folderService.getMyFolders(userId));
    }
}
