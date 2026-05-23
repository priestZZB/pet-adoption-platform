package com.pet.module.mall.model.vo;

/**
 * POI搜索建议VO
 * 用户输入关键词后下拉展示的建议项
 */
public class PoiSuggestionVo {

    /** POI名称（如"深圳湾万象城"） */
    private String name;
    /** 省份 */
    private String province;
    /** 城市 */
    private String city;
    /** 区县 */
    private String district;
    /** 详细地址 */
    private String address;
    /** 显示文本（名称+地址摘要） */
    private String display;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDisplay() { return display; }
    public void setDisplay(String display) { this.display = display; }
}
