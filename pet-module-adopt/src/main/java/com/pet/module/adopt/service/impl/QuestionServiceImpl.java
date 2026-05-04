package com.pet.module.adopt.service.impl;

import com.github.pagehelper.PageHelper;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.adopt.mapper.AdoptQuestionMapper;
import com.pet.module.adopt.model.entity.AdoptQuestion;
import com.pet.module.adopt.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private AdoptQuestionMapper adoptQuestionMapper;

    @Override
    public List<AdoptQuestion> getQuestionList(int page, int size) {
        PageHelper.startPage(page, size);
        return adoptQuestionMapper.selectAll();
    }

    @Override
    public void add(String question, String optionA, String optionB, String optionC, String optionD, String correctAnswer) {
        AdoptQuestion q = new AdoptQuestion();
        q.setQuestion(question);
        q.setOptionA(optionA);
        q.setOptionB(optionB);
        q.setOptionC(optionC);
        q.setOptionD(optionD);
        q.setCorrectAnswer(correctAnswer);
        adoptQuestionMapper.insert(q);
    }

    @Override
    public void update(Long id, String question, String optionA, String optionB, String optionC, String optionD, String correctAnswer) {
        AdoptQuestion q = adoptQuestionMapper.selectById(id);
        if (q == null) {
            throw new BusinessException(ResultCodeEnum.QUESTION_NOT_FOUND);
        }
        AdoptQuestion update = new AdoptQuestion();
        update.setId(id);
        update.setQuestion(question);
        update.setOptionA(optionA);
        update.setOptionB(optionB);
        update.setOptionC(optionC);
        update.setOptionD(optionD);
        update.setCorrectAnswer(correctAnswer);
        adoptQuestionMapper.updateById(update);
    }

    @Override
    public void delete(Long id) {
        if (adoptQuestionMapper.selectById(id) == null) {
            throw new BusinessException(ResultCodeEnum.QUESTION_NOT_FOUND);
        }
        adoptQuestionMapper.deleteById(id);
    }
}