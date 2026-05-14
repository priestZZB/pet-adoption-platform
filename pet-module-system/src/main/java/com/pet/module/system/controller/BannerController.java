package com.pet.module.system.controller;

import com.pet.common.result.Result;
import com.pet.module.system.model.entity.Banner;
import com.pet.module.system.service.BannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "轮播图")
@RestController
@RequestMapping("/api")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @ApiOperation("轮播图列表")
    @GetMapping("/banners")
    public Result<List<Banner>> list() {
        return Result.success(bannerService.getBannerList());
    }
}
