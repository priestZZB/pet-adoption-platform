package com.pet.module.chat.mapper;

import com.pet.module.chat.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMessageMapper {

    int insert(ChatMessage msg);

    /**
     * 某个会话的消息列表（按时间正序）
     */
    List<ChatMessage> selectConversation(@Param("petId") Long petId,
                                          @Param("userId1") Long userId1,
                                          @Param("userId2") Long userId2);

    /**
     * 我的会话列表（去重，每个会话取最新一条消息）
     */
    List<ChatMessage> selectMyConversations(@Param("userId") Long userId);

    /**
     * 未读消息数
     */
    int countUnread(@Param("userId") Long userId);

    /**
     * 标记某个会话的所有消息为已读
     */
    int markAsRead(@Param("petId") Long petId,
                   @Param("senderId") Long senderId,
                   @Param("receiverId") Long receiverId);

    /**
     * 删除双方之间的会话消息
     */
    int deleteByParticipants(@Param("petId") Long petId,
                             @Param("userId1") Long userId1,
                             @Param("userId2") Long userId2);
}
