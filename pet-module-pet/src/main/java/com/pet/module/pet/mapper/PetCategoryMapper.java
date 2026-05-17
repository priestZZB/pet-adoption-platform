package com.pet.module.pet.mapper;

import com.pet.module.pet.model.entity.PetCategory;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PetCategoryMapper {

    int insert(PetCategory category);

    int updateById(PetCategory category);

    int deleteById(Long id);

    PetCategory selectById(Long id);

    List<PetCategory> selectList();

    /** 将 [from, to] 区间内排序号全部 +step（排除 excludeId） */
    int shiftRange(@Param("from") Integer from, @Param("to") Integer to,
                   @Param("step") int step, @Param("excludeId") Long excludeId);
}