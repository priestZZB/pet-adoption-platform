package com.pet.module.adopt.mapper;

import com.pet.module.adopt.model.entity.AdoptApplication;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AdoptApplicationMapper {

    int insert(AdoptApplication application);

    int updateById(AdoptApplication application);

    AdoptApplication selectById(Long id);

    List<AdoptApplication> selectByUserId(Long userId);

    List<AdoptApplication> selectByPetId(Long petId);

    List<AdoptApplication> selectAll(@Param("status") String status);

    /**
     * 统计用户对某宠物的非拒绝申请数（防止重复提交）
     */
    int countActiveByUserAndPet(@Param("userId") Long userId, @Param("petId") Long petId);

    // ========== Dashboard 统计 ==========
    int countAll();

    int countByStatus(@Param("status") String status);
}