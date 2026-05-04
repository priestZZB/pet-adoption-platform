package com.pet.module.pet.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.pet.mapper.PetCategoryMapper;
import com.pet.module.pet.model.entity.PetCategory;
import com.pet.module.pet.model.vo.PetCategoryVo;
import com.pet.module.pet.service.PetCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetCategoryServiceImpl implements PetCategoryService {

    @Autowired
    private PetCategoryMapper petCategoryMapper;

    @Override
    public List<PetCategoryVo> getCategoryList() {
        return petCategoryMapper.selectList().stream().map(c -> {
            PetCategoryVo vo = new PetCategoryVo();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void add(String name, Integer sortOrder) {
        PetCategory category = new PetCategory();
        category.setName(name);
        category.setSortOrder(sortOrder);
        petCategoryMapper.insert(category);
    }

    @Override
    public void update(Long id, String name, Integer sortOrder) {
        PetCategory category = petCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCodeEnum.CATEGORY_NOT_FOUND);
        }
        PetCategory update = new PetCategory();
        update.setId(id);
        update.setName(name);
        update.setSortOrder(sortOrder);
        petCategoryMapper.updateById(update);
    }

    @Override
    public void delete(Long id) {
        if (petCategoryMapper.selectById(id) == null) {
            throw new BusinessException(ResultCodeEnum.CATEGORY_NOT_FOUND);
        }
        petCategoryMapper.deleteById(id);
    }
}