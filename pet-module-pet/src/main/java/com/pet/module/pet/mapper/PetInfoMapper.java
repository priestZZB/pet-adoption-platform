package com.pet.module.pet.mapper;

import com.pet.module.pet.model.entity.PetInfo;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PetInfoMapper {

    int insert(PetInfo petInfo);

    int updateById(PetInfo petInfo);

    int deleteById(Long id);

    PetInfo selectById(Long id);

    List<PetInfo> selectPage(@Param("categoryId") Long categoryId,
                             @Param("keyword") String keyword,
                             @Param("status") String status);

    List<PetInfo> selectByUserId(Long userId);

    List<PetInfo> selectPendingList();

    List<PetInfo> selectReviewedByReviewer(Long reviewerId);

    List<PetInfo> selectByStatusList(@Param("statusList") List<String> statusList);
}