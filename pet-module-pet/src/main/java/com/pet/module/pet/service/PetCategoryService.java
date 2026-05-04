package com.pet.module.pet.service;

import com.pet.module.pet.model.entity.PetCategory;
import com.pet.module.pet.model.vo.PetCategoryVo;
import java.util.List;

public interface PetCategoryService {

    List<PetCategoryVo> getCategoryList();

    void add(String name, Integer sortOrder);

    void update(Long id, String name, Integer sortOrder);

    void delete(Long id);
}