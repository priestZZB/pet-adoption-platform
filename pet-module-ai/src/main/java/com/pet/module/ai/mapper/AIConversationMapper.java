package com.pet.module.ai.mapper;

import com.pet.module.ai.model.entity.AIConversation;
import java.util.List;

public interface AIConversationMapper {

    int insert(AIConversation conversation);

    List<AIConversation> selectByUserId(Long userId);

    int deleteByUserId(Long userId);

    List<AIConversation> selectAll();
}