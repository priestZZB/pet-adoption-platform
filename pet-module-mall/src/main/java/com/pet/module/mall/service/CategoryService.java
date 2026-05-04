package com.pet.module.mall.service;

import com.pet.module.mall.model.entity.MallCategory;
import java.util.List;

public interface CategoryService {

    List<MallCategory> getCategoryList();

    void add(String name, Integer sortOrder);

    void update(Long id, String name, Integer sortOrder);

    void delete(Long id);
}