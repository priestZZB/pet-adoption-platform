package com.pet.module.mall.mapper;

import com.pet.module.mall.model.entity.MallOrderItem;
import java.util.List;

public interface MallOrderItemMapper {

    int insert(MallOrderItem item);

    int insertBatch(List<MallOrderItem> list);

    List<MallOrderItem> selectByOrderId(Long orderId);

    List<MallOrderItem> selectByOrderIds(List<Long> orderIds);
}