package com.pet.module.ai.service.impl;

import com.github.pagehelper.PageHelper;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.ai.mapper.AIConversationMapper;
import com.pet.module.ai.model.dto.ChatDto;
import com.pet.module.ai.model.entity.AIConversation;
import com.pet.module.ai.model.vo.ChatVo;
import com.pet.module.ai.service.AIService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("aiService")
public class AIServiceImpl implements AIService {

    @Value("${pet.ai.api-key:sk-demo}")
    private String apiKey;

    @Value("${pet.ai.model:deepseek-chat}")
    private String model;

    @Value("${pet.ai.api-url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Autowired
    private AIConversationMapper conversationMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public ChatVo chat(Long userId, ChatDto dto) {
        String question = dto.getQuestion();
        if (question == null || question.trim().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "问题不能为空");
        }

        String answer = callDeepSeek(question);

        AIConversation conversation = new AIConversation();
        conversation.setUserId(userId);
        conversation.setQuestion(question);
        conversation.setAnswer(answer);
        conversationMapper.insert(conversation);

        ChatVo vo = new ChatVo();
        BeanUtils.copyProperties(conversation, vo);
        return vo;
    }

    @Override
    public List<ChatVo> getHistory(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<AIConversation> list = conversationMapper.selectByUserId(userId);
        return list.stream().map(c -> {
            ChatVo vo = new ChatVo();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearHistory(Long userId) {
        conversationMapper.deleteByUserId(userId);
    }

    @Override
    public List<AIConversation> getAllRecords() {
        return conversationMapper.selectAll();
    }

    @SuppressWarnings("unchecked")
    private String callDeepSeek(String question) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(Map.of("role", "user", "content", question)),
                    "max_tokens", 1024
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        } catch (Exception e) {
            return "抱歉，AI服务调用失败：" + e.getMessage();
        }
    }
}