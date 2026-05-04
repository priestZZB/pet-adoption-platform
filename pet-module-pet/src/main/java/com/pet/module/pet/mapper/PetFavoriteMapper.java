package com.pet.module.pet.mapper;

import com.pet.module.pet.model.entity.PetFavorite;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PetFavoriteMapper {

    int insert(PetFavorite favorite);

    int deleteByUserAndPet(@Param("userId") Long userId, @Param("petId") Long petId);

    PetFavorite selectByUserAndPet(@Param("userId") Long userId, @Param("petId") Long petId);

    List<PetFavorite> selectByUserId(Long userId);

    int countByPetId(Long petId);
}