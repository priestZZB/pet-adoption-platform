package com.pet.module.pet.model.dto;

public class ReplyDto {

    private String content;
    private Long replyTo;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getReplyTo() { return replyTo; }
    public void setReplyTo(Long replyTo) { this.replyTo = replyTo; }
}
