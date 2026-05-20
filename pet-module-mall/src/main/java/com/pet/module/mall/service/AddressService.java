package com.pet.module.mall.service;

import com.pet.module.mall.model.dto.AddressDto;
import com.pet.module.mall.model.vo.AddressVo;
import java.util.List;

public interface AddressService {

    /**
     * 获取用户的所有收货地址
     */
    List<AddressVo> getUserAddresses(Long userId);

    /**
     * 新增收货地址
     */
    AddressVo add(Long userId, AddressDto dto);

    /**
     * 编辑收货地址
     */
    AddressVo update(Long userId, Long id, AddressDto dto);

    /**
     * 删除收货地址
     */
    void delete(Long userId, Long id);

    /**
     * 设为默认地址
     */
    void setDefault(Long userId, Long id);

    /**
     * 获取用户的默认地址
     */
    AddressVo getDefault(Long userId);

    /**
     * 通过ID获取地址（校验归属）
     */
    AddressVo getById(Long userId, Long id);
}
