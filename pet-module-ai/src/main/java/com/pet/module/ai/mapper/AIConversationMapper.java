package com.pet.module.ai.mapper;

import com.pet.module.ai.model.entity.AIConversation;
import com.pet.module.ai.model.vo.SessionVo;

import java.util.List;

public interface AIConversationMapper {

    int insert(AIConversation conversation);

    List<AIConversation> selectByUserId(Long userId);

    List<AIConversation> selectBySessionId(String sessionId);

    List<AIConversation> selectBySessionIdAdmin(String sessionId);

    List<SessionVo> selectSessionsByUserId(Long userId);

    int deleteByUserId(Long userId);

    int deleteBySessionId(String sessionId, Long userId);

    List<SessionVo> selectAllSessions();

    List<AIConversation> selectAll();
}
