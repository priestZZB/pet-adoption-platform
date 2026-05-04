package com.pet.module.pet.model.dto;

import java.util.List;

/**
 * 编辑宠物请求
 */
public class PetUpdateDto {

    private Long categoryId;
    private String name;
    private String age;
    private String gender;
    private Integer isNeutered;
    private Integer isVaccinated;
    private String healthCert;
    private String personality;
    private String habit;
    private String reason;
    private List<String> images;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getIsNeutered() { return isNeutered; }
    public void setIsNeutered(Integer isNeutered) { this.isNeutered = isNeutered; }
    public Integer getIsVaccinated() { return isVaccinated; }
    public void setIsVaccinated(Integer isVaccinated) { this.isVaccinated = isVaccinated; }
    public String getHealthCert() { return healthCert; }
    public void setHealthCert(String healthCert) { this.healthCert = healthCert; }
    public String getPersonality() { return personality; }
    public void setPersonality(String personality) { this.personality = personality; }
    public String getHabit() { return habit; }
    public void setHabit(String habit) { this.habit = habit; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}