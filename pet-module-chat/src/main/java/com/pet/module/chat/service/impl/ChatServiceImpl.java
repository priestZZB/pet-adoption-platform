package com.pet.module.chat.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.event.NotificationEvent;
import com.pet.common.exception.BusinessException;
import com.pet.module.chat.mapper.ChatMessageMapper;
import com.pet.module.chat.model.entity.ChatMessage;
import com.pet.module.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${pet.chat.violation-limit:3}")
    private int violationLimit;

    // ===== 违禁词检测模式（加强版）=====
    private static final Pattern[] VIOLATION_PATTERNS = new Pattern[]{
            Pattern.compile("1[3-9]\\d{9}"),                                         // 手机号
            Pattern.compile("0\\d{2,3}[- ]?\\d{7,8}"),                               // 座机号
            Pattern.compile("(加)?微信[：: ]?\\S{0,20}"),                              // 微信/加微信
            Pattern.compile("[Ww][Xx][_\\-a-zA-Z0-9]{4,}"),                           // wx_xxx
            Pattern.compile("[Ww]e[Cc]hat", Pattern.CASE_INSENSITIVE),                // WeChat
            Pattern.compile("(加)?[qQ][qQ]"),                                         // qq/加qq
            Pattern.compile("扣扣"),                                                   // 扣扣
            Pattern.compile("(手机|电话|联系)[：:]?\\d+"),                             // 手机/电话/联系
            Pattern.compile("https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"), // 网址
            Pattern.compile("www\\.[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*\\.[a-zA-Z]{2,}"),    // www.xxx.com
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"),       // 邮箱
            Pattern.compile("(vx|v信|薇信|v心|绿色软件)"),                              // 其他变体
    };

    private static final String REDIS_KEY_PREFIX = "chat:violations:";

    @Override
    public Long sendMessage(Long senderId, Long receiverId, Long petId, String content, String imageUrl) {
        // 1. 先检查违禁词（在事务外执行）
        if (content != null && !content.trim().isEmpty()) {
            boolean isAdopted = isPetAdopted(senderId, petId) || isPetAdopted(receiverId, petId);
            if (!isAdopted && containsViolation(content)) {
                handleViolation(senderId);
            }
        }

        // 2. 保存消息
        ChatMessage msg = new ChatMessage();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setPetId(petId);
        msg.setContent(content != null ? content : "");
        msg.setImageUrl(imageUrl);
        msg.setIsRead(0);
        chatMessageMapper.insert(msg);

        return msg.getId();
    }

    /**
     * 检查是否已领养该宠物
     */
    private boolean isPetAdopted(Long userId, Long petId) {
        if (userId == null || petId == null) return false;
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM adopt_application WHERE user_id = ? AND pet_id = ? AND status = 'APPROVED'",
                    Integer.class, userId, petId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("检查领养状态失败 userId={} petId={}", userId, petId, e);
            return false;
        }
    }

    /**
     * 检测文本是否包含违禁模式
     */
    private boolean containsViolation(String text) {
        for (Pattern p : VIOLATION_PATTERNS) {
            if (p.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理违禁发送：计数 + 通知 + 封号
     */
    private void handleViolation(Long userId) {
        String redisKey = REDIS_KEY_PREFIX + userId;

        // 递增违禁次数
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count == null) count = 1L;

        // 站内通知用户
        eventPublisher.publishEvent(new NotificationEvent(
                userId, "CHAT_VIOLATION",
                "消息发送失败",
                "消息包含联系方式等违规内容，已记录。累计违规" + count + "次，超过" + violationLimit + "次将自动禁用账号",
                null));

        if (count >= violationLimit) {
            // 封号
            jdbcTemplate.update("UPDATE sys_user SET status = 0 WHERE id = ?", userId);
            redisTemplate.delete(redisKey);

            // 通知管理员
            List<Long> adminIds = getAdminUserIds();
            for (Long adminId : adminIds) {
                eventPublisher.publishEvent(new NotificationEvent(
                        adminId, "USER_BANNED",
                        "用户因违规已自动封禁",
                        "用户ID:" + userId + " 因多次发送违规内容已自动禁用",
                        userId));
            }

            throw new BusinessException(ResultCodeEnum.USER_DISABLED,
                    "您的账号因多次发送违规内容已被禁用，请联系管理员");
        }

        throw new BusinessException(ResultCodeEnum.BAD_REQUEST,
                "消息包含违规内容（联系方式），已记录，累计违规" + count + "次");
    }

    private List<Long> getAdminUserIds() {
        return jdbcTemplate.queryForList(
                "SELECT u.id FROM sys_user u " +
                "JOIN sys_user_role ur ON u.id = ur.user_id " +
                "JOIN sys_role r ON ur.role_id = r.id " +
                "WHERE r.role_code = 'ADMIN'", Long.class);
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
