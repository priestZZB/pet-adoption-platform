package com.pet.module.adopt.model.vo;

import java.time.LocalDateTime;

/**
 * 答题结果返回（分数+是否通过）
 */
public class ExamResultVo {

    private Integer score;
    private Integer totalQuestions;
    private Integer isPassed;
    private LocalDateTime createdAt;

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Integer getIsPassed() { return isPassed; }
    public void setIsPassed(Integer isPassed) { this.isPassed = isPassed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}