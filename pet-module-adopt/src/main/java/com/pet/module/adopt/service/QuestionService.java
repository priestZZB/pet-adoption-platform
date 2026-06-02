package com.pet.module.adopt.service;

import com.pet.module.adopt.model.entity.AdoptQuestion;
import java.util.List;

public interface QuestionService {

    List<AdoptQuestion> getQuestionList(int page, int size);

    void add(String question, String optionA, String optionB, String optionC, String optionD, String correctAnswer);

    void update(Long id, String question, String optionA, String optionB, String optionC, String optionD, String correctAnswer);

    void delete(Long id);

    /**
     * 批量删除试题
     */
    void batchDelete(List<Long> ids);

    /**
     * 批量导入试题（从 Excel/CSV 解析的行列表）
     */
    int batchImport(List<AdoptQuestion> questions);
}