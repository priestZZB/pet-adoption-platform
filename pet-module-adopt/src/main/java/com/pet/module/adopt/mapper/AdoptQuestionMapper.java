package com.pet.module.adopt.mapper;

import com.pet.module.adopt.model.entity.AdoptQuestion;
import java.util.List;

public interface AdoptQuestionMapper {

    int insert(AdoptQuestion question);

    int updateById(AdoptQuestion question);

    int deleteById(Long id);

    AdoptQuestion selectById(Long id);

    List<AdoptQuestion> selectAll();

    List<AdoptQuestion> selectRandom();
}