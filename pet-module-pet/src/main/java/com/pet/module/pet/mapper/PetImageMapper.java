package com.pet.module.pet.mapper;

import com.pet.module.pet.model.entity.PetImage;
import java.util.List;

public interface PetImageMapper {

    int insert(PetImage petImage);

    int insertBatch(List<PetImage> list);

    int deleteByPetId(Long petId);

    List<PetImage> selectByPetId(Long petId);
}