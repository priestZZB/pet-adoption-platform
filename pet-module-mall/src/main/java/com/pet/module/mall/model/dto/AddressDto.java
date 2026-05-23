package com.pet.module.mall.model.dto;

/**
 * 收货地址新增/编辑请求
 */
public class AddressDto {

    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String specificPlace;      // 具体位置（小区/酒店/地标，必填）
    private String roomNo;             // 门牌号（选填）
    private String detailAddress;      // 兼容旧前端（可选，新前端传specificPlace+roomNo）
    private Integer isDefault;

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getSpecificPlace() { return specificPlace; }
    public void setSpecificPlace(String specificPlace) { this.specificPlace = specificPlace; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public String getDetailAddress() { return detailAddress; }
    public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }
}
