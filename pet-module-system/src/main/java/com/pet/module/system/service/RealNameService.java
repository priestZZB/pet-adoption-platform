package com.pet.module.system.service;

/**
 * 实名认证服务（调用第三方人脸比对API）
 */
public interface RealNameService {

    /**
     * 人脸比对认证（使用base64图片）
     *
     * @param name      姓名
     * @param idNumber  身份证号
     * @param imageBase64 人脸图片base64
     * @return true=认证通过，false=不通过
     */
    boolean verifyWithImage(String name, String idNumber, String imageBase64);

    /**
     * 人脸比对认证（使用图片URL）
     *
     * @param name     姓名
     * @param idNumber 身份证号
     * @param imageUrl 人脸图片URL
     * @return true=认证通过，false=不通过
     */
    boolean verifyWithImageUrl(String name, String idNumber, String imageUrl);
}
