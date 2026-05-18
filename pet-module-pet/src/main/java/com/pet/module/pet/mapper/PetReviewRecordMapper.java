package com.pet.module.pet.mapper;

import com.pet.module.pet.model.entity.PetReviewRecord;
import java.util.List;

public interface PetReviewRecordMapper {

    int insert(PetReviewRecord record);

    List<PetReviewRecord> selectByPetId(Long petId);

    List<Long> selectReviewerPetIds(Long reviewerId);

    /**
     * 查询审核历史（已去重，每个宠物只取最新一条）
     */
    List<com.pet.module.pet.model.vo.ReviewHistoryVo> selectReviewHistoryByReviewer(Long reviewerId);
}