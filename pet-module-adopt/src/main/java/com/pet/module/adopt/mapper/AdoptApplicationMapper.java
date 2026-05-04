package com.pet.module.adopt.mapper;

import com.pet.module.adopt.model.entity.AdoptApplication;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AdoptApplicationMapper {

    int insert(AdoptApplication application);

    int updateById(AdoptApplication application);

    AdoptApplication selectById(Long id);

    List<AdoptApplication> selectByUserId(Long userId);

    List<AdoptApplication> selectByPetId(Long petId);

    List<AdoptApplication> selectAll(@Param("status") String status);
}