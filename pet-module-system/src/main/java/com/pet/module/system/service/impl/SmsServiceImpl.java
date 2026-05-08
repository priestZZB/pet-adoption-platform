package com.pet.module.system.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.system.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现 — 调用第三方三网106短信API
 * 验证码存储于 Redis，有效期 5 分钟
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int CODE_LENGTH = 6;

    @Value("${pet.third-party.appcode}")
    private String appcode;

    @Value("${pet.third-party.sms.url}")
    private String smsUrl;

    @Value("${pet.third-party.sms.template-id}")
    private String templateId;

    @Value("${pet.third-party.sms.sign-id}")
    private String signId;

    /** 开发模式：不调用真实短信API，控制台输出验证码 */
    @Value("${pet.sms.mock:true}")
    private boolean smsMock;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String sendCode(String phone) {
        // 1. 生成6位随机验证码
        String code = RandomUtil.randomNumbers(CODE_LENGTH);

        // 2. 开发模式：跳过真实短信发送，控制台输出验证码
        if (smsMock) {
            log.info("========== 📱 短信验证码（模拟）==========");
            log.info("手机号: {}", phone);
            log.info("验证码: {}", code);
            log.info("有效期: {}分钟", CODE_EXPIRE_MINUTES);
            log.info("========================================");
            // 控制台额外输出，方便一眼看到
            System.out.println("\n========== 📱 短信验证码（模拟）==========");
            System.out.println("手机号: " + phone);
            System.out.println("验证码: " + code);
            System.out.println("========================================\n");
        } else {
            // 生产模式：调用第三方短信API发送
            try {
                String param = "**code**:" + code + ",**minute**:" + CODE_EXPIRE_MINUTES;
                String url = UriComponentsBuilder.fromHttpUrl(smsUrl)
                        .queryParam("mobile", phone)
                        .queryParam("templateId", templateId)
                        .queryParam("smsSignId", signId)
                        .queryParam("param", param)
                        .build()
                        .toUriString();

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "APPCODE " + appcode);

                HttpEntity<String> request = new HttpEntity<>(headers);
                String response = restTemplate.postForObject(url, request, String.class);

                log.info("短信发送响应: phone={}, response={}", phone, response);

                if (response != null && !response.isEmpty()) {
                    if (response.contains("\"code\":\"0\"")) {
                        log.info("短信发送成功: phone={}", phone);
                    } else {
                        log.warn("短信API返回非成功状态: {}", response);
                    }
                }

            } catch (org.springframework.web.client.HttpClientErrorException e) {
                String errorBody = e.getResponseBodyAsString();
                log.error("短信API调用失败: status={}, body={}", e.getStatusCode(), errorBody);
                if (e.getStatusCode().value() == 403) {
                    if (errorBody != null && errorBody.contains("Quota Exhausted")) {
                        throw new BusinessException(ResultCodeEnum.UNKNOWN_ERROR, "短信余额不足，请联系管理员");
                    }
                    throw new BusinessException(ResultCodeEnum.UNKNOWN_ERROR, "短信服务认证失败");
                }
                throw new BusinessException(ResultCodeEnum.UNKNOWN_ERROR, "短信发送失败");
            } catch (Exception e) {
                log.error("短信发送异常 phone={}", phone, e);
                throw new BusinessException(ResultCodeEnum.UNKNOWN_ERROR, "短信发送失败，请稍后再试");
            }
        }

        // 3. 验证码存入 Redis，5分钟有效
        redisTemplate.opsForValue().set(
                SMS_CODE_PREFIX + phone,
                code,
                CODE_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        log.info("短信验证码已存入Redis: phone={}, code={}", phone, code);
        return code;
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        String key = SMS_CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            return false;
        }
        boolean match = storedCode.equals(code);
        if (match) {
            // 验证通过后删除，防止重复使用
            redisTemplate.delete(key);
        }
        return match;
    }
}
