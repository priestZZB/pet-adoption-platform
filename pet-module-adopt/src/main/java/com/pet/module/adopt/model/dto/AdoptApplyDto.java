package com.pet.module.adopt.model.dto;

/**
 * 提交领养申请
 */
public class AdoptApplyDto {

    private Long petId;
    private String livingEnv;
    private String petExp;
    private String commitment;

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getLivingEnv() { return livingEnv; }
    public void setLivingEnv(String livingEnv) { this.livingEnv = livingEnv; }
    public String getPetExp() { return petExp; }
    public void setPetExp(String petExp) { this.petExp = petExp; }
    public String getCommitment() { return commitment; }
    public void setCommitment(String commitment) { this.commitment = commitment; }
}