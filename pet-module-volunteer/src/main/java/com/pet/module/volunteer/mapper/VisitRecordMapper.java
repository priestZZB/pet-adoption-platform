package com.pet.module.volunteer.mapper;

import com.pet.module.volunteer.model.entity.VisitRecord;
import java.util.List;

public interface VisitRecordMapper {

    int insert(VisitRecord record);

    VisitRecord selectById(Long id);

    List<VisitRecord> selectByVolunteerId(Long volunteerId);
}