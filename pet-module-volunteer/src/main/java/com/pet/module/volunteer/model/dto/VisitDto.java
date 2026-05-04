package com.pet.module.volunteer.model.dto;

import java.util.List;

/**
 * 提交走访记录请求
 */
public class VisitDto {

    private Long petId;
    private String visitDate;
    private String content;
    private List<String> images;

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}