package com.pet.module.mall.mapper;

import com.pet.module.mall.model.entity.ShippingAddress;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ShippingAddressMapper {

    int insert(ShippingAddress address);

    int updateById(ShippingAddress address);

    int deleteById(Long id);

    ShippingAddress selectById(Long id);

    List<ShippingAddress> selectByUserId(Long userId);

    // 获取用户的默认地址
    ShippingAddress selectDefaultByUserId(Long userId);

    // 将该用户所有地址取消默认
    int clearDefaultByUserId(Long userId);

    // 将该用户某个地址设为默认
    int setDefault(@Param("id") Long id, @Param("userId") Long userId);
}
