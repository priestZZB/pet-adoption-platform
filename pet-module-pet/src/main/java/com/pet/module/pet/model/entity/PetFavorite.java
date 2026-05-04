package com.pet.module.pet.model.entity;

import java.time.LocalDateTime;

/**
 * 宠物收藏表 pet_favorite
 */
public class PetFavorite {

    private Long id;
    private Long userId;
    private Long petId;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}