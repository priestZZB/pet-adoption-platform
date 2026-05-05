package com.pet.module.system.service.impl;

import com.pet.module.system.service.RealNameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 实名认证实现 — 调用第三方公安人脸比对API
 * API要求：Content-Type = application/x-www-form-urlencoded
 */
@Slf4j
@Service
public class RealNameServiceImpl implements RealNameService {

    @Value("${pet.third-party.appcode}")
    private String appcode;

    @Value("${pet.third-party.face.url}")
    private String faceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean verifyWithImage(String name, String idNumber, String imageBase64) {
        return doVerify(name, idNumber, imageBase64, null);
    }

    @Override
    public boolean verifyWithImageUrl(String name, String idNumber, String imageUrl) {
        return doVerify(name, idNumber, null, imageUrl);
    }

    /**
     * 调用第三方人脸比对API（表单提交）
     */
    private boolean doVerify(String name, String idNumber, String image, String imageUrl) {
        try {
            // Header: APPCODE 认证 + form-urlencoded
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "APPCODE " + appcode);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Body: 表单参数
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("name", name);
            body.add("id_number", idNumber);
            body.add("auto_rotate", "true");

            if (image != null && !image.isEmpty()) {
                body.add("image", image);
            } else if (imageUrl != null && !imageUrl.isEmpty()) {
                body.add("image_url", imageUrl);
            }

            log.info("人脸比对请求: name={}, id_number={}, hasImage={}, hasImageUrl={}",
                    name, idNumber, image != null, imageUrl != null);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // 发送POST请求，捕获字符串响应
            ResponseEntity<String> response = restTemplate.exchange(
                    faceUrl, HttpMethod.POST, request, String.class);

            log.info("人脸比对响应状态: {}, body={}", response.getStatusCode(), response.getBody());

            // 解析响应
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
     * 成功：{ "status":"OK", "result_code":1001, "score":"66" }
     * 失败：{ "status":"INVALID_ARGUMENT", "reason":"..." }
     */
    private boolean parseFaceResponse(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return false;
        }

        // 检查状态
        if (responseBody.contains("\"status\":\"OK\"") || responseBody.contains("\"status\": \"OK\"")) {
            // 解析 result_code
            if (responseBody.contains("\"result_code\":1001") || responseBody.contains("\"result_code\": 1001")) {
                log.info("人脸比对通过: result_code=1001 (同一人)");
                return true;
            }
            // 也支持 score >= 45 做二次判断
            if (responseBody.contains("\"result_code\":1002") || responseBody.contains("\"result_code\": 1002")) {
                // 不确定时看score
                try {
                    String scoreStr = extractJsonValue(responseBody, "score");
                    if (scoreStr != null) {
                        double score = Double.parseDouble(scoreStr);
                        log.info("人脸比对score={}, result_code=1002", score);
                        return score >= 45;
                    }
                } catch (Exception ignored) {}
            }
        }

        // 错误信息打印
        if (responseBody.contains("\"reason\"")) {
            String reason = extractJsonValue(responseBody, "reason");
            log.warn("人脸比对API返回错误: {}", reason);
        }

        return false;
    }

    /**
     * 简单JSON字段值提取（不用依赖Jackson/Gson解析整个JSON）
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
            // 字符串值
            int end = start + 1;
            while (end < json.length() && json.charAt(end) != '"') {
                if (json.charAt(end) == '\\') end++; // 跳过转义
                end++;
            }
            return json.substring(start + 1, end);
        } else {
            // 数字/布尔值
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end))
                    || json.charAt(end) == '.' || json.charAt(end) == '-')) {
                end++;
            }
            return json.substring(start, end);
        }
    }
}
