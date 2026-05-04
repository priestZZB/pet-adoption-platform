package com.pet.module.adopt.mapper;

import com.pet.module.adopt.model.entity.AdoptExamRecord;
import java.util.List;

public interface AdoptExamRecordMapper {

    int insert(AdoptExamRecord record);

    AdoptExamRecord selectLatestByUserId(Long userId);

    List<AdoptExamRecord> selectByUserId(Long userId);
}