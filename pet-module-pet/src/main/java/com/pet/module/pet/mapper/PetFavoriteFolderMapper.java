package com.pet.module.pet.mapper;

import com.pet.module.pet.model.entity.PetFavoriteFolder;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PetFavoriteFolderMapper {

    int insert(PetFavoriteFolder folder);

    int updateById(PetFavoriteFolder folder);

    int deleteById(@Param("id") Long id);

    int deleteByUserId(@Param("userId") Long userId);

    PetFavoriteFolder selectById(@Param("id") Long id);

    List<PetFavoriteFolder> selectByUserId(@Param("userId") Long userId);

    int countByNameAndUser(@Param("userId") Long userId, @Param("name") String name);
}
