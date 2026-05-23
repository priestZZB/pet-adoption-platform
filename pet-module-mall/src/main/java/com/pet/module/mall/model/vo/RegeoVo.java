package com.pet.module.mall.model.vo;

/**
 * 逆地理编码返回VO
 * 根据经纬度定位获得的地址信息
 */
public class RegeoVo {

    /** 省份 */
    private String province;
    /** 城市 */
    private String city;
    /** 区县 */
    private String district;
    /** 最近POI名称（如：深圳湾万象城） */
    private String nearestPoi;
    /** POI详细地址 */
    private String poiAddress;
    /** 格式化地址（省市区+POI） */
    private String formattedAddress;

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getNearestPoi() { return nearestPoi; }
    public void setNearestPoi(String nearestPoi) { this.nearestPoi = nearestPoi; }
    public String getPoiAddress() { return poiAddress; }
    public void setPoiAddress(String poiAddress) { this.poiAddress = poiAddress; }
    public String getFormattedAddress() { return formattedAddress; }
    public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
}
