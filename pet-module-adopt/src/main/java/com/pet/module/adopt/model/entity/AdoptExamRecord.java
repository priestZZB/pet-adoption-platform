package com.pet.module.adopt.model.entity;

import java.time.LocalDateTime;

/**
 * 答题记录表 adopt_exam_record
 */
public class AdoptExamRecord {

    private Long id;
    private Long userId;
    private Integer score;
    private Integer totalQuestions;
    private Integer isPassed;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Integer getIsPassed() { return isPassed; }
    public void setIsPassed(Integer isPassed) { this.isPassed = isPassed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}