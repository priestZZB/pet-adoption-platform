package com.pet.module.system.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.system.mapper.BannerMapper;
import com.pet.module.system.model.entity.Banner;
import com.pet.module.system.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public List<Banner> getBannerList() {
        return bannerMapper.selectAll();
    }

    @Override
    public void add(Banner banner) {
        bannerMapper.insert(banner);
    }

    @Override
    public void update(Banner banner) {
        Banner existing = bannerMapper.selectById(banner.getId());
        if (existing == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "轮播图不存在");
        }
        bannerMapper.updateById(banner);
    }

    @Override
    public void delete(Long id) {
        Banner existing = bannerMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "轮播图不存在");
        }
        bannerMapper.deleteById(id);
    }
}
