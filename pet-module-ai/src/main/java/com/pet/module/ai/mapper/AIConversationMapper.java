package com.pet.module.ai.mapper;

import com.pet.module.ai.model.entity.AIConversation;
import com.pet.module.ai.model.vo.SessionVo;

import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AIConversationMapper {

    int insert(AIConversation conversation);

    List<AIConversation> selectByUserId(Long userId);

    List<AIConversation> selectBySessionId(String sessionId);

    List<AIConversation> selectBySessionIdAdmin(String sessionId);

    List<SessionVo> selectSessionsByUserId(Long userId);

    int deleteByUserId(Long userId);

    int deleteBySessionId(@Param("sessionId") String sessionId, @Param("userId") Long userId);

    List<SessionVo> selectAllSessions();

    List<AIConversation> selectAll();
}
