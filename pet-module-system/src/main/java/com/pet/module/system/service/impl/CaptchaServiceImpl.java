package com.pet.module.system.service.impl;

import com.pet.module.system.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 行为验证码实现 — 调用怜花滑块验证码API
 * API要求：Content-Type = application/x-www-form-urlencoded
 * 认证方式：Authorization: APPCODE
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Value("${pet.third-party.appcode}")
    private String appcode;

    @Value("${pet.third-party.captcha.app-id}")
    private String captchaAppId;

    @Value("${pet.third-party.captcha.app-secret}")
    private String captchaAppSecret;

    @Value("${pet.third-party.captcha.verify-url}")
    private String verifyUrl;

    @Value("${pet.captcha.enabled:true}")
    private boolean captchaEnabled;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean verify(String ticket, String randstr, String captchaSign, String sourceIp) {
        // 开发模式跳过验证码校验（演示时再开启）
        if (!captchaEnabled) {
            log.info("行为验证码已禁用（开发模式），跳过验证");
            return true;
        }

        // 参数基础校验
        if (ticket == null || ticket.isEmpty()) {
            log.warn("行为验证码验证失败: ticket为空");
            return false;
        }
        if (randstr == null || randstr.isEmpty()) {
            log.warn("行为验证码验证失败: randstr为空");
            return false;
        }
        if (captchaSign == null || captchaSign.isEmpty()) {
            log.warn("行为验证码验证失败: captchaSign为空");
            return false;
        }

        try {
            // Header: APPCODE 认证 + form-urlencoded
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "APPCODE " + appcode);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Body: 表单参数
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("captchaAppId", captchaAppId);
            body.add("ticket", ticket);
            body.add("randstr", randstr);
            body.add("captchaSign", captchaSign);
            if (sourceIp != null && !sourceIp.isEmpty()) {
                body.add("sourceIp", sourceIp);
            }

            log.info("行为验证码验证请求: captchaAppId={}, ticket={}, randstr={}, captchaSign={}, sourceIp={}",
                    captchaAppId, ticket, randstr, captchaSign, sourceIp);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // 发送POST请求
            ResponseEntity<String> response = restTemplate.exchange(
                    verifyUrl, HttpMethod.POST, request, String.class);

            log.info("行为验证码验证响应: status={}, body={}", response.getStatusCode(), response.getBody());

            // 解析响应
            return parseCaptchaResponse(response.getBody());

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("行为验证码API HTTP错误: status={}, responseBody={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("行为验证码API调用异常", e);
            return false;
        }
    }

    /**
     * 解析行为验证码响应
     * 成功：{ "code": 200, "msg": "成功", "taskNo": "xxx" }
     * 失败：{ "code": 301, "msg": "验证码不正确" }
     *      { "code": 302, "msg": "验证码超时,请重新获取." }
     *      { "code": 303, "msg": "来源IP不匹配" }
     *      { "code": 400, "msg": "请求入参错误" }
     *      { "code": 500, "msg": "系统维护中，请稍候再试" }
     */
    private boolean parseCaptchaResponse(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            log.warn("行为验证码响应为空");
            return false;
        }

        // 检查 code 是否为 200
        if (responseBody.contains("\"code\":200") || responseBody.contains("\"code\": 200")) {
            log.info("行为验证码验证通过");
            return true;
        }

        // 失败时打印错误信息
        String msg = extractJsonValue(responseBody, "msg");
        log.warn("行为验证码验证失败: code={}, msg={}", extractJsonValue(responseBody, "code"), msg);
        return false;
    }

    /**
     * 简单JSON字段值提取（不用依赖Jackson/Gson解析整个JSON）
     * 与 RealNameServiceImpl 中使用相同工具方法
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
                if (json.charAt(end) == '\\') end++;
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
