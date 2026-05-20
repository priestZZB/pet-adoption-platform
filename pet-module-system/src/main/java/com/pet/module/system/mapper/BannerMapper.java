package com.pet.module.system.mapper;

import com.pet.module.system.model.entity.Banner;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BannerMapper {

    int insert(Banner banner);

    int updateById(Banner banner);

    int deleteById(Long id);

    Banner selectById(Long id);

    List<Banner> selectAll();

    /**
     * 排序号搬移：将 [from, to] 范围内的 sort_order 统一加上 step
     */
    int shiftRange(@Param("from") Integer from, @Param("to") Integer to,
                   @Param("step") Integer step, @Param("excludeId") Long excludeId);
}
