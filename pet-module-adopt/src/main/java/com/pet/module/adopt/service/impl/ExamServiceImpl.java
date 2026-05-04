package com.pet.module.adopt.service.impl;

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

        int score = 0;
        int total = dto.getAnswers().size();

        for (ExamSubmitDto.AnswerItem item : dto.getAnswers()) {
            AdoptQuestion q = adoptQuestionMapper.selectById(item.getQuestionId());
            if (q != null && q.getCorrectAnswer().equals(item.getAnswer())) {
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