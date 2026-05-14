package com.pet.module.ai.service;

import com.github.pagehelper.PageInfo;
import com.pet.module.ai.model.dto.ChatDto;
import com.pet.module.ai.model.vo.ChatVo;
import com.pet.module.ai.model.entity.AIConversation;
import com.pet.module.ai.model.vo.SessionVo;

import java.util.List;

public interface AIService {

    ChatVo chat(Long userId, ChatDto dto);

    List<ChatVo> getHistory(Long userId, int page, int size);

    void clearHistory(Long userId);

    List<SessionVo> getSessions(Long userId);

    List<ChatVo> getSessionMessages(String sessionId, boolean includeDeleted);

    void deleteSession(Long userId, String sessionId);

    List<SessionVo> getAllSessions();

    PageInfo<AIConversation> getAllRecords(int page, int size);
}
