package com.pet.module.system.mapper;

import com.pet.module.system.model.entity.SysFeedback;
import java.util.List;

public interface FeedbackMapper {

    int insert(SysFeedback feedback);

    int updateById(SysFeedback feedback);

    SysFeedback selectById(Long id);

    List<SysFeedback> selectByUserId(Long userId);

    List<SysFeedback> selectAll();

    List<SysFeedback> selectPending();

    int countPending();
}