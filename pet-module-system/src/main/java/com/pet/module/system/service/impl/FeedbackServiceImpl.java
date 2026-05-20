package com.pet.module.system.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.event.NotificationEvent;
import com.pet.common.exception.BusinessException;
import com.pet.module.system.mapper.FeedbackMapper;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.system.model.entity.SysFeedback;
import com.pet.module.system.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void submit(Long userId, String content, String images) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "反馈内容不能为空");
        }
        SysFeedback feedback = new SysFeedback();
        feedback.setUserId(userId);
        feedback.setContent(content);
        feedback.setImages(images);
        feedbackMapper.insert(feedback);

        // 发送站内通知给所有管理员
        String summary = content.length() > 50 ? content.substring(0, 50) + "…" : content;
        List<Long> adminIds = userMapper.selectAdminUserIds();
        for (Long adminId : adminIds) {
            eventPublisher.publishEvent(new NotificationEvent(
                    adminId, "FEEDBACK_NEW",
                    "新的意见反馈",
                    "用户提交了新的反馈：" + summary,
                    feedback.getId()));
        }
    }

    @Override
    public List<SysFeedback> getMyFeedbacks(Long userId) {
        return feedbackMapper.selectByUserId(userId);
    }

    @Override
    public List<SysFeedback> getAll() {
        return feedbackMapper.selectAll();
    }

    @Override
    public void reply(Long id, String reply) {
        SysFeedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(ResultCodeEnum.FEEDBACK_NOT_FOUND);
        }
        SysFeedback update = new SysFeedback();
        update.setId(id);
        update.setReply(reply);
        update.setStatus(1);
        feedbackMapper.updateById(update);

        String summary = reply.length() > 30 ? reply.substring(0, 30) + "…" : reply;
        eventPublisher.publishEvent(new NotificationEvent(
                feedback.getUserId(), "FEEDBACK_REPLY",
                "反馈已回复",
                "管理员回复了你的反馈：" + summary,
                id));
    }
}