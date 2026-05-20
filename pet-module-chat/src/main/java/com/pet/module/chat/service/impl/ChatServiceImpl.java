package com.pet.module.chat.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.event.NotificationEvent;
import com.pet.common.exception.BusinessException;
import com.pet.module.chat.mapper.ChatMessageMapper;
import com.pet.module.chat.model.entity.ChatMessage;
import com.pet.module.chat.service.ChatSSEService;
import com.pet.module.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    @Autowired
    private ChatSSEService chatSSEService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${pet.chat.violation-limit:3}")
    private int violationLimit;

    // ===== 违禁词检测模式（加强版，与前端同步）=====
    private static final Pattern[] VIOLATION_PATTERNS = new Pattern[]{
            // ── 基础联系方式 ──
            Pattern.compile("1[3-9]\\d{9}"),                                              // 手机号
            Pattern.compile("0\\d{2,3}[- ]?\\d{7,8}"),                                   // 座机号
            Pattern.compile("https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"), // 网址
            Pattern.compile("www\\.[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*\\.[a-zA-Z]{2,}"),        // www.xxx.com
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"),           // 邮箱

            // ── 微信相关导流词 ──
            Pattern.compile("(加)?[微Vv][信新心号]"),                                        // 微信/微新/微心/微号/v信
            Pattern.compile("[vVxX]{2}|[wW][xX]"),                                          // vx VX wx WX
            Pattern.compile("加微|来微|去微|微聊"),                                           // 加微/来微/去微/微聊
            Pattern.compile("绿泡泡"),                                                        // 绿泡泡
            Pattern.compile("wechat", Pattern.CASE_INSENSITIVE),                             // WeChat

            // ── QQ 相关导流词 ──
            Pattern.compile("扣扣|寇寇|球球|秋秋"),                                            // QQ 中文变体
            Pattern.compile("(加)?[qQ]{2}"),                                                 // QQ / 加qq

            // ── 私下转移交流类 ──
            Pattern.compile("私下[聊说]|私底下[聊说]"),                                        // 私下聊/私底下
            Pattern.compile("私聊|私我|私发|私联|私加"),                                        // 私聊/私我/私发
            Pattern.compile("单独[聊说]|悄悄[说聊]|偷偷[说聊]"),                                // 单独/悄悄/偷偷

            // ── 站外跨平台转移词 ──
            Pattern.compile("站外|出平台|换平台|脱离平台|换地方"),                                // 站外/出平台/换平台
            Pattern.compile("外面聊|出去聊|场外聊"),                                            // 外面聊/出去聊/场外聊
            Pattern.compile("别的平台|其他平台"),                                              // 别的平台/其他平台

            // ── 引导加好友导流词 ──
            Pattern.compile("加我|来加我|加一下|互加|加好友|扩列"),                              // 加好友/扩列
            Pattern.compile("来找我[聊细说]?"),                                                // 来找我/来找我聊
            Pattern.compile("联系我|单独联系"),                                                // 联系我

            // ── 主页资料引流词 ──
            Pattern.compile("看主页|主页自取"),                                                // 看主页
            Pattern.compile("看我(资料|简介|名字|头像|备注)"),                                 // 看我xxx
            Pattern.compile("资料里有"),                                                      // 资料里有

            // ── 发送推送导流词 ──
            Pattern.compile("发(你|我|给你|联系方式|号码)"),                                   // 发你/发我/发给你/发联系方式
            Pattern.compile("推(你|我)"),                                                     // 推你/推我

            // ── 其他社交平台导流词 ──
            Pattern.compile("抖音|快手|小红书|红书|微博|哔哩|闲鱼"),                            // 社交平台
            Pattern.compile("B\\s*站"),                                                     // B站
            Pattern.compile("淘宝|拼多多|虎牙|斗鱼|网易云"),                                    // 电商/直播平台

            // ── 号码联系方式导流词 ──
            Pattern.compile("手机号|电话号|电话号码|座机"),                                     // 号码联系方式
            Pattern.compile("号码|联系方式|联络方式"),                                         // 联系方式

            // ── 隐晦暗语导流词 ──
            Pattern.compile("绿色软件|绿色图标"),                                              // 绿色软件/绿色图标
            Pattern.compile("聊天软件|常用软件|常用聊天"),                                      // 聊天软件
            Pattern.compile("老地方|老位置"),                                                  // 老地方/老位置
            Pattern.compile("别处[聊说]?"),                                                   // 别处/别处聊
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

        // 3. WebSocket STOMP 实时推送（推送给发送者和接收者）
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(), "/queue/chat", msg);
        messagingTemplate.convertAndSendToUser(
                senderId.toString(), "/queue/chat", msg);

        // 4. SSE 实时推送（兜底，兼容不支持 WebSocket 的环境）
        chatSSEService.pushNewMessage(receiverId, msg);
        chatSSEService.pushNewMessage(senderId, msg);
        chatSSEService.pushConversationUpdate(receiverId);
        chatSSEService.pushConversationUpdate(senderId);
        try {
            int unreadCount = chatMessageMapper.countUnread(receiverId);
            chatSSEService.pushUnreadCount(receiverId, unreadCount);
        } catch (Exception ignored) {}

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

    @Override
    public void deleteConversation(Long petId, Long userId, Long otherUserId) {
        chatMessageMapper.deleteByParticipants(petId, userId, otherUserId);
    }
}
