package com.pet.module.volunteer.service;

import com.pet.module.volunteer.model.dto.VisitDto;
import com.pet.module.volunteer.model.vo.VisitVo;
import java.util.List;

public interface VisitService {

    void add(Long volunteerId, VisitDto dto);

    List<VisitVo> getMyVisits(Long volunteerId, int page, int size);

    VisitVo getVisitDetail(Long id);
}