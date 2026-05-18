package com.pet.module.chat.service.impl;

import com.pet.module.chat.mapper.ChatMessageMapper;
import com.pet.module.chat.model.entity.ChatMessage;
import com.pet.module.chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional
    public Long sendMessage(Long senderId, Long receiverId, Long petId, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setPetId(petId);
        msg.setContent(content);
        msg.setIsRead(0);
        chatMessageMapper.insert(msg);

        return msg.getId();
    }

    @Override
    public List<ChatMessage> getConversation(Long petId, Long userId, Long otherUserId) {
        return chatMessageMapper.selectConversation(petId, userId, otherUserId);
    }

    @Override
    public List<ChatMessage> getMyConversations(Long userId) {
        return chatMessageMapper.selectMyConversations(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return chatMessageMapper.countUnread(userId);
    }

    @Override
    public void markAsRead(Long petId, Long senderId, Long receiverId) {
        chatMessageMapper.markAsRead(petId, senderId, receiverId);
    }
}
