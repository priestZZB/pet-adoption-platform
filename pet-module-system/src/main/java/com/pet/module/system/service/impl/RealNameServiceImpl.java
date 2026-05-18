package com.pet.module.system.service.impl;

import com.pet.module.system.service.RealNameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 实名认证实现
 *   - 活体检测（H5人脸动作采集）：杭州快证签
 *   - 人脸比对（公安实名认证）：杭州快证签
 * API要求：Content-Type = application/x-www-form-urlencoded
 * 认证方式：Authorization: APPCODE
 */
@Slf4j
@Service
public class RealNameServiceImpl implements RealNameService {

    @Value("${pet.third-party.appcode}")
    private String appcode;

    // ---- 人脸比对（公安实名认证）----
    @Value("${pet.third-party.face.url}")
    private String faceUrl;

    // ---- 活体检测 H5 ----
    @Value("${pet.third-party.liveness.token-url}")
    private String livenessTokenUrl;

    @Value("${pet.third-party.liveness.result-url}")
    private String livenessResultUrl;

    /**
     * mock=true  → 模拟模式：不调真实API，校验数据库即返回通过
     * mock=false → 真实模式：调杭州快证签人脸比对API进行实名认证
     */
    @Value("${pet.realname.mock:true}")
    private boolean mockMode;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean verifyWithImage(String name, String idNumber, String imageBase64) {
        return doVerify(name, idNumber, imageBase64, null);
    }

    @Override
    public boolean verifyWithImageUrl(String name, String idNumber, String imageUrl) {
        return doVerify(name, idNumber, null, imageUrl);
    }

    // ==================================================================
    //  人脸比对（公安实名认证）
    // ==================================================================

    /**
     * 调用第三方人脸比对API（表单提交）
     */
    private boolean doVerify(String name, String idNumber, String image, String imageUrl) {
        // 模拟模式：直接返回通过
        if (mockMode) {
            log.info("人脸比对模拟模式: name={}, idcard={} → 模拟通过", name, idNumber);
            return true;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "APPCODE " + appcode);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("name", name);
            body.add("idcard", idNumber);

            if (image != null && !image.isEmpty()) {
                body.add("image", image);
            } else if (imageUrl != null && !imageUrl.isEmpty()) {
                body.add("url", imageUrl);
            } else {
                log.warn("人脸比对失败: 未提供图片");
                return false;
            }

            log.info("人脸比对请求: name={}, idcard={}, hasImage={}, hasUrl={}",
                    name, idNumber, image != null, imageUrl != null);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    faceUrl, HttpMethod.POST, request, String.class);

            log.info("人脸比对响应状态: {}, body={}", response.getStatusCode(), response.getBody());

            return parseFaceResponse(response.getBody());

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("人脸比对HTTP错误: status={}, responseBody={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("人脸比对API调用异常", e);
            return false;
        }
    }

    /**
     * 解析人脸比对响应
     * 成功：{ "success": true, "code": 200, "data": { "resultCode": 1001, "score": 0.99, "msg": "人脸判断为同一人" } }
     * 失败：{ "success": false, "code": 400, "msg": "请输入有效的身份证号" }
     *
     * resultCode:
     *   1001 = 同一人（score 0.45-1）
     *   1002 = 不确定（score 0.4-0.45）
     *   1003 = 不同人（score 0-0.4）
     *   1004 = 无法识别（无分数，身份证姓名不一致等）
     */
    private boolean parseFaceResponse(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return false;
        }

        // 检查 success 和 code
        if (responseBody.contains("\"success\":true") || responseBody.contains("\"success\": true")) {
            // 解析 resultCode（从 data 对象提取，避免取到根级别的同名key）
            String resultCode = extractJsonValueFromData(responseBody, "resultCode");
            if (resultCode != null && resultCode.equals("1001")) {
                log.info("人脸比对通过: resultCode=1001 (同一人)");
                return true;
            }
            if (resultCode != null && resultCode.equals("1002")) {
                // 不确定时看 score（>= 0.45 通过）
                String scoreStr = extractJsonValueFromData(responseBody, "score");
                if (scoreStr != null) {
                    try {
                        double score = Double.parseDouble(scoreStr);
                        log.info("人脸比对score={}, resultCode=1002", score);
                        return score >= 0.45;
                    } catch (NumberFormatException ignored) {}
                }
            }
            // 失败（1003/1004）：从 data 取真实错误原因
            // 根级别的"msg"是"成功"，data里的"msg"才是原因（如"身份证号码姓名不一致"）
            if (resultCode != null) {
                String dataMsg = extractJsonValueFromData(responseBody, "msg");
                log.warn("人脸比对未通过: resultCode={}, reason={}", resultCode, dataMsg != null ? dataMsg : "未知");
                return false;
            }
        }

        // success=false：直接从根节点取错误信息
        String rootMsg = extractJsonValue(responseBody, "msg");
        log.warn("人脸比对请求失败: {}", rootMsg != null ? rootMsg : responseBody);
        return false;
    }

    // ==================================================================
    //  活体检测 H5
    // ==================================================================

    @Override
    public Map<String, String> getLivenessToken(String returnUrl) {
        Map<String, String> result = new HashMap<>();
        // 模拟模式：返回模拟数据
        if (mockMode) {
            log.info("活体检测Token模拟模式: returnUrl={}", returnUrl);
            result.put("url", "");
            result.put("orderNo", "mock_" + System.currentTimeMillis());
            return result;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "APPCODE " + appcode);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("returnUrl", returnUrl);
            body.add("style", "1");           // 摇头/点头 + 眨眼/张嘴 随机组合
            body.add("foreLiveOn", "true");   // 开启前端小模型活体算法
            body.add("backLiveOn", "false");  // 关闭云端大模型
            body.add("showFail", "true");     // 失败展示结果页
            body.add("showSuccess", "false"); // 成功不展示（直接跳转returnUrl）

            log.info("活体检测Token请求: returnUrl={}", returnUrl);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    livenessTokenUrl, HttpMethod.POST, request, String.class);

            log.info("活体检测Token响应: status={}, body={}", response.getStatusCode(), response.getBody());

            String respBody = response.getBody();
            if (respBody != null) {
                // 解析响应 { success: true, code: 200, data: { url, orderNo } }
                if (respBody.contains("\"success\":true") || respBody.contains("\"success\": true")) {
                    String url = extractJsonValueFromData(respBody, "url");
                    String orderNo = extractJsonValueFromData(respBody, "orderNo");
                    if (url != null) {
                        result.put("url", url);
                        result.put("orderNo", orderNo != null ? orderNo : "");
                        return result;
                    }
                }
            }
            log.warn("活体检测Token获取失败: {}", respBody);
        } catch (Exception e) {
            log.error("活体检测Token获取异常", e);
        }
        result.put("error", "获取活体检测页面失败");
        return result;
    }

    @Override
    public Map<String, Object> checkLivenessResult(String orderNo, String hbId) {
        Map<String, Object> result = new HashMap<>();
        // 模拟模式：返回模拟通过
        if (mockMode && orderNo != null && orderNo.startsWith("mock_")) {
            log.info("活体检测结果模拟模式: orderNo={} → 模拟通过", orderNo);
            result.put("result", 0);
            result.put("desc", "模拟通过");
            result.put("orderNo", orderNo);
            result.put("faceImageUrl", "mock_url");
            return result;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "APPCODE " + appcode);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("orderNo", orderNo);
            if (hbId != null && !hbId.isEmpty()) {
                body.add("hbId", hbId);
            }

            log.info("活体检测结果查询: orderNo={}, hbId={}", orderNo, hbId);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    livenessResultUrl, HttpMethod.POST, request, String.class);

            log.info("活体检测结果响应: status={}, body={}", response.getStatusCode(), response.getBody());

            String respBody = response.getBody();
            if (respBody != null) {
                // 解析响应 { success: true, code: 200, data: { result, desc, orderNo, faceImageUrl } }
                if (respBody.contains("\"success\":true") || respBody.contains("\"success\": true")) {
                    String resultCode = extractJsonValueFromData(respBody, "result");
                    String desc = extractJsonValueFromData(respBody, "desc");
                    String respOrderNo = extractJsonValueFromData(respBody, "orderNo");
                    String faceImageUrl = extractJsonValueFromData(respBody, "faceImageUrl");

                    // result: 0/1=通过, 2=进行中
                    if (resultCode != null) {
                        result.put("result", Integer.parseInt(resultCode));
                    } else {
                        result.put("result", -1);
                    }
                    result.put("desc", desc != null ? desc : "");
                    result.put("orderNo", respOrderNo != null ? respOrderNo : orderNo);
                    // 活体通过时（result=0），会返回人脸图片URL
                    if (faceImageUrl != null) {
                        result.put("faceImageUrl", faceImageUrl);
                    }
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("活体检测结果查询异常", e);
        }
        result.put("result", -1);
        result.put("desc", "查询活体检测结果失败");
        return result;
    }

    // ==================================================================
    //  JSON 解析工具方法
    // ==================================================================

    /**
     * 从JSON根级别提取字段值
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int idx = json.indexOf(searchKey);
        if (idx == -1) {
            searchKey = "\"" + key + "\": ";
            idx = json.indexOf(searchKey);
        }
        if (idx == -1) return null;

        int start = idx + searchKey.length();
        if (start >= json.length()) return null;

        char firstChar = json.charAt(start);
        if (firstChar == '"') {
            int end = start + 1;
            while (end < json.length() && json.charAt(end) != '"') {
                if (json.charAt(end) == '\\') end++;
                end++;
            }
            return json.substring(start + 1, end);
        } else {
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end))
                    || json.charAt(end) == '.' || json.charAt(end) == '-')) {
                end++;
            }
            return json.substring(start, end);
        }
    }

    /**
     * 从JSON的"data"对象中提取字段值
     * 搜索模式: "data":{ ... "key":"value" ... }
     */
    private String extractJsonValueFromData(String json, String key) {
        // 先找到 data: 开始位置
        String dataKey = "\"data\":";
        int dataIdx = json.indexOf(dataKey);
        if (dataIdx == -1) {
            dataKey = "\"data\": ";
            dataIdx = json.indexOf(dataKey);
        }
        if (dataIdx == -1) return null;

        // 从 data 之后搜索 key
        String searchKey = "\"" + key + "\":";
        int idx = json.indexOf(searchKey, dataIdx);
        if (idx == -1) {
            searchKey = "\"" + key + "\": ";
            idx = json.indexOf(searchKey, dataIdx);
        }
        if (idx == -1) return null;

        int start = idx + searchKey.length();
        if (start >= json.length()) return null;

        char firstChar = json.charAt(start);
        if (firstChar == '"') {
            int end = start + 1;
            while (end < json.length() && json.charAt(end) != '"') {
                if (json.charAt(end) == '\\') end++;
                end++;
            }
            return json.substring(start + 1, end);
        } else {
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end))
                    || json.charAt(end) == '.' || json.charAt(end) == '-')) {
                end++;
            }
            return json.substring(start, end);
        }
    }
}
