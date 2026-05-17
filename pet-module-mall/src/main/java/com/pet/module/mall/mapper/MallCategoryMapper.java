package com.pet.module.mall.mapper;

import com.pet.module.mall.model.entity.MallCategory;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MallCategoryMapper {

    int insert(MallCategory category);

    int updateById(MallCategory category);

    int deleteById(Long id);

    MallCategory selectById(Long id);

    List<MallCategory> selectList();

    int shiftRange(@Param("from") Integer from, @Param("to") Integer to,
                   @Param("step") int step, @Param("excludeId") Long excludeId);
}
