package com.pet.module.pet.mapper;

import com.pet.module.pet.model.entity.PetReviewRecord;
import java.util.List;

public interface PetReviewRecordMapper {

    int insert(PetReviewRecord record);

    List<PetReviewRecord> selectByPetId(Long petId);

    List<Long> selectReviewerPetIds(Long reviewerId);
}