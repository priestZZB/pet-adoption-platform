package com.pet.module.pet.mapper;

import com.pet.module.pet.model.entity.PetCategory;
import java.util.List;

public interface PetCategoryMapper {

    int insert(PetCategory category);

    int updateById(PetCategory category);

    int deleteById(Long id);

    PetCategory selectById(Long id);

    List<PetCategory> selectList();
}