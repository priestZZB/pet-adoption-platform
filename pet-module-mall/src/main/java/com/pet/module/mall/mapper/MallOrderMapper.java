package com.pet.module.mall.mapper;

import com.pet.module.mall.model.entity.MallOrder;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MallOrderMapper {

    int insert(MallOrder order);

    int updateById(MallOrder order);

    MallOrder selectById(Long id);

    MallOrder selectByOrderNo(String orderNo);

    List<MallOrder> selectByUserId(@Param("userId") Long userId, @Param("status") String status);

    List<MallOrder> selectAll(@Param("status") String status);
}