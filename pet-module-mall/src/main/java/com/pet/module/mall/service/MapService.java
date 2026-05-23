package com.pet.module.mall.service;

import com.pet.module.mall.model.vo.PoiSuggestionVo;
import com.pet.module.mall.model.vo.RegeoVo;

import java.util.List;

/**
 * 地图服务接口 — 高德Web服务API
 * 提供逆地理编码（定位→地址）和POI搜索建议功能
 */
public interface MapService {

    /**
     * 逆地理编码：根据经纬度获取地址信息
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 地址信息（省/市/区/最近POI）
     */
    RegeoVo reverseGeocode(double lng, double lat);

    /**
     * POI搜索建议：根据关键词搜索地点
     *
     * @param keywords 搜索关键词
     * @param city     限定城市（可选）
     * @return POI建议列表
     */
    List<PoiSuggestionVo> placeSuggest(String keywords, String city);
}
