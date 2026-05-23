package com.pet.module.mall.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.mall.model.vo.PoiSuggestionVo;
import com.pet.module.mall.model.vo.RegeoVo;
import com.pet.module.mall.service.MapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 地图服务实现 — 调用高德Web服务API
 *
 * 需要申请高德Web服务API Key（非JS API）
 * 免费额度：逆地理编码 30万次/月, 输入提示 30万次/月
 */
@Slf4j
@Service
public class MapServiceImpl implements MapService {

    @Value("${pet.map.amap-key}")
    private String amapKey;

    @Value("${pet.map.regeo-url:https://restapi.amap.com/v3/geocode/regeo}")
    private String regeoUrl;

    @Value("${pet.map.inputtips-url:https://restapi.amap.com/v3/assistant/inputtips}")
    private String inputtipsUrl;

    /**
     * mock=true → 模拟模式：不调高德API，返回固定数据
     * mock=false → 真实模式：调高德Web服务API
     */
    @Value("${pet.map.mock:true}")
    private boolean mapMock;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public RegeoVo reverseGeocode(double lng, double lat) {
        if (mapMock) {
            log.info("========== 📍 逆地理编码（模拟模式）==========");
            log.info("坐标: {}, {} — 跳过真实API调用", lng, lat);
            log.info("==============================================");
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST,
                    "开发模式下定位功能不可用，请手动填写地址");
        }

        try {
            String location = lng + "," + lat;
            String url = UriComponentsBuilder.fromHttpUrl(regeoUrl)
                    .queryParam("key", amapKey)
                    .queryParam("location", location)
                    .queryParam("radius", 500)
                    .queryParam("extensions", "all")
                    .build()
                    .toUriString();

            log.debug("调用高德逆地理编码: location={}", location);
            String response = restTemplate.getForObject(url, String.class);
            log.debug("高德逆地理编码响应: {}", response);

            JSONObject json = JSONObject.parseObject(response);
            String status = json.getString("status");

            if (!"1".equals(status)) {
                String info = json.getString("info");
                log.warn("高德逆地理编码失败: status={}, info={}", status, info);
                throw new BusinessException(ResultCodeEnum.UNKNOWN_ERROR, "定位失败：" + info);
            }

            JSONObject regeocode = json.getJSONObject("regeocode");
            JSONObject addressComponent = regeocode.getJSONObject("addressComponent");

            RegeoVo vo = new RegeoVo();
            vo.setProvince(normalizeRegionName(addressComponent.getString("province")));
            vo.setCity(normalizeRegionName(addressComponent.getString("city")));
            vo.setDistrict(normalizeRegionName(addressComponent.getString("district")));

            // 获取最近POI
            JSONArray pois = regeocode.getJSONArray("pois");
            if (pois != null && !pois.isEmpty()) {
                JSONObject nearest = pois.getJSONObject(0);
                vo.setNearestPoi(nearest.getString("name"));
                vo.setPoiAddress(nearest.getString("address"));
                vo.setFormattedAddress(
                        vo.getProvince() + vo.getCity() + vo.getDistrict() +
                        nearest.getString("name") + nearest.getString("address")
                );
            } else {
                vo.setFormattedAddress(
                        vo.getProvince() + vo.getCity() + vo.getDistrict()
                );
            }

            log.info("逆地理编码成功: {} → {}", location, vo.getFormattedAddress());
            return vo;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("高德逆地理编码异常: lng={}, lat={}", lng, lat, e);
            throw new BusinessException(ResultCodeEnum.UNKNOWN_ERROR, "定位服务异常，请稍后再试");
        }
    }

    @Override
    public List<PoiSuggestionVo> placeSuggest(String keywords, String city) {
        if (mapMock) {
            log.info("========== 🔍 POI搜索（模拟模式）==========");
            log.info("关键词: {} — 跳过真实API调用", keywords);
            log.info("==========================================");
            return new ArrayList<>();
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(inputtipsUrl)
                    .queryParam("key", amapKey)
                    .queryParam("keywords", keywords)
                    .queryParam("datatype", "poi");

            if (city != null && !city.isEmpty()) {
                builder.queryParam("city", city);
            }

            String url = builder.build().toUriString();

            log.debug("调用高德输入提示: keywords={}, city={}", keywords, city);
            String response = restTemplate.getForObject(url, String.class);

            JSONObject json = JSONObject.parseObject(response);
            String status = json.getString("status");

            if (!"1".equals(status)) {
                String info = json.getString("info");
                log.warn("高德输入提示失败: status={}, info={}", status, info);
                throw new BusinessException(ResultCodeEnum.UNKNOWN_ERROR, "搜索失败：" + info);
            }

            JSONArray tips = json.getJSONArray("tips");
            List<PoiSuggestionVo> result = new ArrayList<>();

            if (tips != null) {
                for (int i = 0; i < tips.size(); i++) {
                    JSONObject tip = tips.getJSONObject(i);

                    PoiSuggestionVo vo = new PoiSuggestionVo();
                    String name = tip.getString("name");
                    String province = normalizeRegionName(tip.getString("pname"));
                    String cityName = normalizeRegionName(tip.getString("cityname"));
                    String district = normalizeRegionName(tip.getString("adname"));
                    String address = tip.getString("address");

                    // 跳过空名称或无省市区的结果
                    if (name == null || name.isEmpty() ||
                        province == null || province.isEmpty()) {
                        continue;
                    }

                    vo.setName(name);
                    vo.setProvince(province);
                    vo.setCity(cityName);
                    vo.setDistrict(district);
                    vo.setAddress(address != null ? address : "");

                    // 构建显示文本：名称 · 区县+地址
                    String display = name;
                    if (district != null && !district.isEmpty()) {
                        display += " · " + district;
                    }
                    if (address != null && !address.isEmpty()) {
                        display += address;
                    }
                    vo.setDisplay(display);

                    result.add(vo);
                }
            }

            log.info("POI搜索成功: keywords={}, 结果数={}", keywords, result.size());
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("高德输入提示异常: keywords={}", keywords, e);
            throw new BusinessException(ResultCodeEnum.UNKNOWN_ERROR, "搜索服务异常，请稍后再试");
        }
    }

    /**
     * 规范化地区名称
     * 高德API在某些情况下返回"市辖区"或空值，这里做适配
     */
    private String normalizeRegionName(String name) {
        if (name == null || name.isEmpty()) return "";
        // 市辖区 → 城市名（由调用方处理）
        if ("市辖区".equals(name) || "省直辖县级行政区划".equals(name)) {
            return "";
        }
        // "广东省" → "广东省" 或 "广东" → 补齐省（暂不处理，region.js匹配时会宽松处理）
        return name;
    }


}
