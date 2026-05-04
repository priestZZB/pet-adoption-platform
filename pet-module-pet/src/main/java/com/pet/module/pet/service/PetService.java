package com.pet.module.pet.service;

import com.pet.module.pet.model.dto.PetPublishDto;
import com.pet.module.pet.model.dto.PetUpdateDto;
import com.pet.module.pet.model.vo.PetDetailVo;
import com.pet.module.pet.model.vo.PetListVo;
import java.util.List;

public interface PetService {

    List<PetListVo> getPetList(Long categoryId, String keyword, String status, int page, int size);

    PetDetailVo getPetDetail(Long petId);

    Long publish(Long userId, PetPublishDto dto);

    void update(Long userId, Long petId, PetUpdateDto dto);

    void offline(Long userId, Long petId);

    List<PetListVo> getUserPets(Long userId);

    List<PetListVo> getPendingPets();

    List<PetListVo> getReviewedByUser(Long reviewerId);

    void adminUpdateStatus(Long petId, String status);

    PetDetailVo getPetDetailForAdmin(Long petId);
}