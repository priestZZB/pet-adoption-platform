package com.pet.module.ai.service;

import com.pet.module.ai.model.dto.ChatDto;
import com.pet.module.ai.model.vo.ChatVo;
import com.pet.module.ai.model.entity.AIConversation;
import java.util.List;

public interface AIService {

    ChatVo chat(Long userId, ChatDto dto);

    List<ChatVo> getHistory(Long userId, int page, int size);

    void clearHistory(Long userId);

    List<AIConversation> getAllRecords();
}