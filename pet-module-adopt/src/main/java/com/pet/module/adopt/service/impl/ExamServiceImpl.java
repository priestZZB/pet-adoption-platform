package com.pet.module.adopt.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.adopt.mapper.AdoptExamRecordMapper;
import com.pet.module.adopt.mapper.AdoptQuestionMapper;
import com.pet.module.adopt.model.dto.ExamSubmitDto;
import com.pet.module.adopt.model.entity.AdoptExamRecord;
import com.pet.module.adopt.model.entity.AdoptQuestion;
import com.pet.module.adopt.model.vo.ExamResultVo;
import com.pet.module.adopt.model.vo.QuestionVo;
import com.pet.module.adopt.service.ExamService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private AdoptQuestionMapper adoptQuestionMapper;

    @Autowired
    private AdoptExamRecordMapper adoptExamRecordMapper;

    @Override
    public List<QuestionVo> startExam() {
        List<AdoptQuestion> questions = adoptQuestionMapper.selectRandom();
        if (questions == null || questions.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.QUESTION_NOT_FOUND, "题库为空，请联系管理员添加试题");
        }
        return questions.stream().map(q -> {
            QuestionVo vo = new QuestionVo();
            BeanUtils.copyProperties(q, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExamResultVo submitExam(Long userId, ExamSubmitDto dto) {
        List<AdoptQuestion> questions = adoptQuestionMapper.selectRandom();
        // 查出所有题目的正确答案用于评分
        List<Long> questionIds = dto.getAnswers().stream()
                .map(ExamSubmitDto.AnswerItem::getQuestionId)
                .collect(Collectors.toList());

        if (dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "请先答题再提交");
        }

        int score = 0;
        int total = dto.getAnswers().size();

        for (ExamSubmitDto.AnswerItem item : dto.getAnswers()) {
            if (item.getQuestionId() == null || item.getAnswer() == null) {
                throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "答题参数不完整");
            }
            AdoptQuestion q = adoptQuestionMapper.selectById(item.getQuestionId());
            if (q == null) {
                throw new BusinessException(ResultCodeEnum.QUESTION_NOT_FOUND, "试题不存在: " + item.getQuestionId());
            }
            if (q.getCorrectAnswer().equals(item.getAnswer())) {
                score++;
            }
        }

        int isPassed = (score == total) ? 1 : 0;

        AdoptExamRecord record = new AdoptExamRecord();
        record.setUserId(userId);
        record.setScore(score);
        record.setTotalQuestions(total);
        record.setIsPassed(isPassed);
        adoptExamRecordMapper.insert(record);

        ExamResultVo vo = new ExamResultVo();
        vo.setScore(score);
        vo.setTotalQuestions(total);
        vo.setIsPassed(isPassed);
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }

    @Override
    public List<ExamResultVo> getHistory(Long userId) {
        List<AdoptExamRecord> records = adoptExamRecordMapper.selectByUserId(userId);
        return records.stream().map(r -> {
            ExamResultVo vo = new ExamResultVo();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}