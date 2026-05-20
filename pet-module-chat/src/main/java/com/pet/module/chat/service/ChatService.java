package com.pet.module.chat.service;

import com.pet.module.chat.model.entity.ChatMessage;
import java.util.List;

public interface ChatService {

    /**
     * 发送消息
     */
    Long sendMessage(Long senderId, Long receiverId, Long petId, String content, String imageUrl);

    /**
     * 获取某个会话的消息列表
     */
    List<ChatMessage> getConversation(Long petId, Long userId, Long otherUserId);

    /**
     * 获取我的会话列表
     */
    List<ChatMessage> getMyConversations(Long userId);

    /**
     * 获取未读消息数
     */
    int getUnreadCount(Long userId);

    /**
     * 标记已读
     */
    void markAsRead(Long petId, Long senderId, Long receiverId);
}
