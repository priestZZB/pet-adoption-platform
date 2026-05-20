package com.pet.module.mall.model.dto;

/**
 * 结算请求（用于创建订单时的收货信息）
 */
public class CheckoutDto {

    private Long addressId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
}