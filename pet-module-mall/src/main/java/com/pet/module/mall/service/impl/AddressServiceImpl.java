package com.pet.module.mall.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.mall.mapper.ShippingAddressMapper;
import com.pet.module.mall.model.dto.AddressDto;
import com.pet.module.mall.model.entity.ShippingAddress;
import com.pet.module.mall.model.vo.AddressVo;
import com.pet.module.mall.service.AddressService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private ShippingAddressMapper addressMapper;

    @Override
    public List<AddressVo> getUserAddresses(Long userId) {
        List<ShippingAddress> list = addressMapper.selectByUserId(userId);
        return list.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressVo add(Long userId, AddressDto dto) {
        // 如果设为默认，先清除其他默认
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            addressMapper.clearDefaultByUserId(userId);
        }

        ShippingAddress address = new ShippingAddress();
        address.setUserId(userId);
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());

        // 拼接完整地址
        String full = dto.getProvince() + dto.getCity() + dto.getDistrict() + dto.getDetailAddress();
        address.setReceiverAddress(full);

        // 如果没有传isDefault，则判断是否是第一条（第一条自动默认）
        if (dto.getIsDefault() != null) {
            address.setIsDefault(dto.getIsDefault());
        } else {
            List<ShippingAddress> existing = addressMapper.selectByUserId(userId);
            address.setIsDefault(existing.isEmpty() ? 1 : 0);
        }

        addressMapper.insert(address);
        return convertToVo(address);
    }

    @Override
    @Transactional
    public AddressVo update(Long userId, Long id, AddressDto dto) {
        ShippingAddress exist = addressMapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "地址不存在");
        }

        // 如果设为默认，先清除其他默认
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            addressMapper.clearDefaultByUserId(userId);
        }

        ShippingAddress update = new ShippingAddress();
        update.setId(id);
        update.setUserId(userId);
        update.setReceiverName(dto.getReceiverName());
        update.setReceiverPhone(dto.getReceiverPhone());
        update.setProvince(dto.getProvince());
        update.setCity(dto.getCity());
        update.setDistrict(dto.getDistrict());
        update.setDetailAddress(dto.getDetailAddress());

        String full = dto.getProvince() + dto.getCity() + dto.getDistrict() + dto.getDetailAddress();
        update.setReceiverAddress(full);

        if (dto.getIsDefault() != null) {
            update.setIsDefault(dto.getIsDefault());
        }

        addressMapper.updateById(update);
        return getById(userId, id);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        ShippingAddress address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "地址不存在");
        }

        addressMapper.deleteById(id);

        // 如果删除的是默认地址，将剩余地址中最近的一个设为默认
        if (address.getIsDefault() == 1) {
            List<ShippingAddress> remaining = addressMapper.selectByUserId(userId);
            if (!remaining.isEmpty()) {
                addressMapper.setDefault(remaining.get(0).getId(), userId);
            }
        }
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long id) {
        ShippingAddress address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "地址不存在");
        }

        addressMapper.clearDefaultByUserId(userId);
        addressMapper.setDefault(id, userId);
    }

    @Override
    public AddressVo getDefault(Long userId) {
        ShippingAddress address = addressMapper.selectDefaultByUserId(userId);
        return address == null ? null : convertToVo(address);
    }

    @Override
    public AddressVo getById(Long userId, Long id) {
        ShippingAddress address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "地址不存在");
        }
        return convertToVo(address);
    }

    private AddressVo convertToVo(ShippingAddress address) {
        AddressVo vo = new AddressVo();
        BeanUtils.copyProperties(address, vo);
        return vo;
    }
}
