package com.pet.module.mall.mapper;

import com.pet.module.mall.model.entity.MallProduct;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MallProductMapper {

    int insert(MallProduct product);

    int updateById(MallProduct product);

    int deleteById(Long id);

    MallProduct selectById(Long id);

    List<MallProduct> selectPage(@Param("categoryId") Long categoryId);

    List<MallProduct> selectAll();
}