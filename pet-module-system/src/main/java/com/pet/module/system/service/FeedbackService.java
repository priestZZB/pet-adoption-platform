package com.pet.module.system.service;

import com.pet.module.system.model.entity.SysFeedback;
import java.util.List;

public interface FeedbackService {

    void submit(Long userId, String content, String images);

    List<SysFeedback> getMyFeedbacks(Long userId);

    List<SysFeedback> getAll();

    void reply(Long id, String reply);
}