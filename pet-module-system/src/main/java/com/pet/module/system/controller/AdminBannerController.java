package com.pet.module.system.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.system.model.entity.Banner;
import com.pet.module.system.service.BannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Log("轮播管理")
@Api(tags = "管理员-轮播管理")
@RestController
@RequestMapping("/api/admin/banners")
@RequireRole("ADMIN")
public class AdminBannerController {

    @Autowired
    private BannerService bannerService;

    @ApiOperation("轮播图列表（管理员）")
    @GetMapping
    public Result<java.util.List<Banner>> list() {
        return Result.success(bannerService.getBannerList());
    }

    @ApiOperation("新增轮播图")
    @PostMapping
    public Result<String> add(@RequestBody Banner banner) {
        bannerService.add(banner);
        return Result.success("新增成功");
    }

    @ApiOperation("编辑轮播图")
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        bannerService.update(banner);
        return Result.success("修改成功");
    }

    @ApiOperation("删除轮播图")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return Result.success("删除成功");
    }
}
