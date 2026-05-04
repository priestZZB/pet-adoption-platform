package com.pet.module.mall.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.mall.mapper.MallCategoryMapper;
import com.pet.module.mall.model.entity.MallCategory;
import com.pet.module.mall.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private MallCategoryMapper mallCategoryMapper;

    @Override
    public List<MallCategory> getCategoryList() {
        return mallCategoryMapper.selectList();
    }

    @Override
    public void add(String name, Integer sortOrder) {
        MallCategory category = new MallCategory();
        category.setName(name);
        category.setSortOrder(sortOrder);
        mallCategoryMapper.insert(category);
    }

    @Override
    public void update(Long id, String name, Integer sortOrder) {
        MallCategory category = mallCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCodeEnum.CATEGORY_NOT_FOUND);
        }
        MallCategory update = new MallCategory();
        update.setId(id);
        update.setName(name);
        update.setSortOrder(sortOrder);
        mallCategoryMapper.updateById(update);
    }

    @Override
    public void delete(Long id) {
        if (mallCategoryMapper.selectById(id) == null) {
            throw new BusinessException(ResultCodeEnum.CATEGORY_NOT_FOUND);
        }
        mallCategoryMapper.deleteById(id);
    }
}