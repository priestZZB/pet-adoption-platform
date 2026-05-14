package com.pet.module.system.mapper;

import com.pet.module.system.model.entity.Banner;
import java.util.List;

public interface BannerMapper {

    int insert(Banner banner);

    int updateById(Banner banner);

    int deleteById(Long id);

    Banner selectById(Long id);

    List<Banner> selectAll();
}
