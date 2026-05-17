package com.pet.module.system.service;

import java.util.Map;

/**
 * 实名认证服务（活体检测 + 人脸比对）
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

    /**
     * 获取活体检测H5页面Token
     *
     * @param returnUrl 活体采集完成后跳转的页面URL
     * @return { url: H5页面地址, orderNo: 订单号 }
     */
    Map<String, String> getLivenessToken(String returnUrl);

    /**
     * 查询活体检测结果
     *
     * @param orderNo 活体检测订单号
     * @param hbId    活体ID（可选，传空字符串）
     * @return { result: 检测结果码, desc: 描述, orderNo: 订单号 }
     */
    Map<String, Object> checkLivenessResult(String orderNo, String hbId);
}
