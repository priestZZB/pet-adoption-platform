package com.pet.module.pet.service;

import com.pet.module.pet.model.dto.PetReviewDto;

public interface PetReviewService {

    void firstReview(Long reviewerId, Long petId, PetReviewDto dto);

    void finalReview(Long adminId, Long petId, PetReviewDto dto);
}