package com.pet.module.pet.mapper;

import com.pet.module.pet.model.entity.PetInfo;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PetInfoMapper {

    int insert(PetInfo petInfo);

    int updateById(PetInfo petInfo);

    /**
     * 乐观锁更新：仅当 status = #{expectStatus} 时更新
     * 返回 0 表示竞态条件触发（他人已修改），用于防并发审核
     */
    int updateStatusIfPending(@Param("id") Long id,
                               @Param("newStatus") String newStatus,
                               @Param("expectStatus") String expectStatus,
                               @Param("reviewRemark") String reviewRemark);

    int deleteById(Long id);

    PetInfo selectById(Long id);

    List<PetInfo> selectPage(@Param("categoryId") Long categoryId,
                             @Param("keyword") String keyword,
                             @Param("status") String status);

    List<PetInfo> selectByUserId(Long userId);

    List<PetInfo> selectPendingList();

    List<PetInfo> selectReviewedByReviewer(Long reviewerId);

    List<PetInfo> selectByStatusList(@Param("statusList") List<String> statusList);

    // ========== Dashboard 统计 ==========
    int countAll();

    int countByStatus(@Param("status") String status);
}