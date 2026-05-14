package com.pet.module.adopt.service;

import com.pet.module.adopt.model.dto.AdoptApplyDto;
import com.pet.module.adopt.model.vo.AdoptApplyVo;
import java.util.List;

public interface AdoptService {

    void apply(Long userId, AdoptApplyDto dto);

    List<AdoptApplyVo> getMyApplications(Long userId);

    AdoptApplyVo getApplicationDetail(Long id);

    List<AdoptApplyVo> getAllApplications(String status, int page, int size);

    AdoptApplyVo getAdminApplicationDetail(Long id);

    void adminReview(Long id, String action);

    List<AdoptApplyVo> getPetApplications(Long petId);

    void donorReview(Long id, String action);
}