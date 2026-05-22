package com.pet.module.mall.mapper;

import com.pet.module.mall.model.entity.MallCart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MallCartMapper {

    /**
     * 插入或更新购物车（有则加数量，无则新增）
     */
    int upsert(MallCart cart);

    MallCart selectByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    List<MallCart> selectByUserId(Long userId);

    int deleteByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    int deleteByUserId(Long userId);
}
