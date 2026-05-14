package com.pet.module.system.service;

import com.pet.module.system.model.entity.Banner;
import java.util.List;

public interface BannerService {

    List<Banner> getBannerList();

    void add(Banner banner);

    void update(Banner banner);

    void delete(Long id);
}
