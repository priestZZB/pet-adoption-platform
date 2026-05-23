package com.pet.module.mall.controller;

import com.pet.common.result.Result;
import com.pet.module.mall.model.vo.PoiSuggestionVo;
import com.pet.module.mall.model.vo.RegeoVo;
import com.pet.module.mall.service.MapService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地图服务Controller — 定位和POI搜索
 * 前端通过此接口代理调用高德Web服务API
 */
@Api(tags = "地图服务")
@RestController
@RequestMapping("/api/map")
public class MapController {

    @Autowired
    private MapService mapService;

    @ApiOperation("逆地理编码：根据经纬度获取地址信息")
    @PostMapping("/reverse-geocode")
    public Result<RegeoVo> reverseGeocode(@RequestBody RegeoRequest request) {
        return Result.success(mapService.reverseGeocode(request.getLng(), request.getLat()));
    }

    @ApiOperation("POI搜索建议：根据关键词搜索地点")
    @GetMapping("/place-suggest")
    public Result<List<PoiSuggestionVo>> placeSuggest(
            @RequestParam String keywords,
            @RequestParam(required = false) String city) {
        return Result.success(mapService.placeSuggest(keywords, city));
    }

    /**
     * 逆地理编码请求体
     */
    public static class RegeoRequest {
        private double lng;
        private double lat;

        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
    }
}
