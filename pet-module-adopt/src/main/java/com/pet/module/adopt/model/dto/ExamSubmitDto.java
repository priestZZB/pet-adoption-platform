package com.pet.module.adopt.model.dto;

import java.util.List;

/**
 * 提交答题答案
 */
public class ExamSubmitDto {

    private List<AnswerItem> answers;

    public List<AnswerItem> getAnswers() { return answers; }
    public void setAnswers(List<AnswerItem> answers) { this.answers = answers; }

    /**
     * 单题答案
     */
    public static class AnswerItem {
        private Long questionId;
        private String answer;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }
}