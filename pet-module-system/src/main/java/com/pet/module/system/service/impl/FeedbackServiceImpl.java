package com.pet.module.system.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.system.mapper.FeedbackMapper;
import com.pet.module.system.model.entity.SysFeedback;
import com.pet.module.system.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

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
    }
}