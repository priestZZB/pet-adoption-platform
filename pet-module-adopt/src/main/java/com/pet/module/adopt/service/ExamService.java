package com.pet.module.adopt.service;

import com.pet.module.adopt.model.dto.ExamSubmitDto;
import com.pet.module.adopt.model.vo.ExamResultVo;
import com.pet.module.adopt.model.vo.QuestionVo;
import java.util.List;

public interface ExamService {

    List<QuestionVo> startExam();

    ExamResultVo submitExam(Long userId, ExamSubmitDto dto);

    List<ExamResultVo> getHistory(Long userId);
}