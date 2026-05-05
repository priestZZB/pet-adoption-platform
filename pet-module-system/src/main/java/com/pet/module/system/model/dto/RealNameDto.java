package com.pet.module.system.model.dto;

/**
 * 实名认证请求
 */
public class RealNameDto {

    private String realName;
    private String idCard;
    private String phone;
    /** 人脸图片base64（前端拍照上传，与imageUrl二选一） */
    private String image;
    /** 人脸图片URL（服务器直接可访问，与image二选一） */
    private String imageUrl;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}