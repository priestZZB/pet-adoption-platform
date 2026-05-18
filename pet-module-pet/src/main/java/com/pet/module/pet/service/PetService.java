package com.pet.module.pet.service;

import com.pet.module.pet.model.dto.PetPublishDto;
import com.pet.module.pet.model.dto.PetUpdateDto;
import com.pet.module.pet.model.vo.PetDetailVo;
import com.pet.module.pet.model.vo.PetListVo;
import com.pet.module.pet.model.vo.PetTimelineEvent;
import com.pet.module.pet.model.vo.ReviewHistoryVo;
import java.util.List;

public interface PetService {

    List<PetListVo> getPetList(Long categoryId, String keyword, String status, int page, int size);

    PetDetailVo getPetDetail(Long petId, Long userId);

    Long publish(Long userId, PetPublishDto dto);

    void update(Long userId, Long petId, PetUpdateDto dto);

    void offline(Long userId, Long petId);

    /**
     * 撤回送养（仅未被任何志愿者审核时可撤回，重置为PENDING）
     */
    void withdraw(Long userId, Long petId);

    List<PetListVo> getUserPets(Long userId);

    List<PetListVo> getPendingPets();

    /**
     * 获取待审核列表，排除指定用户发布的宠物
     */
    List<PetListVo> getPendingPets(Long excludeUserId);

    List<ReviewHistoryVo> getReviewedByUser(Long reviewerId);

    /**
     * 获取宠物的事件时间线（发布/编辑/审核等）
     */
    List<PetTimelineEvent> getPetTimeline(Long petId);

    void adminUpdateStatus(Long petId, String status);

    PetDetailVo getPetDetailForAdmin(Long petId);

    List<com.pet.module.pet.model.vo.PetSelectVo> getSelectablePets();
}