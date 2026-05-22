package com.pet.module.mall.mapper;

import com.pet.module.mall.model.entity.ProductReview;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductReviewMapper {

    int insert(ProductReview review);

    ProductReview selectById(Long id);

    ProductReview selectByOrderItemId(Long orderItemId);

    /** 原始评价（非追加） */
    List<ProductReview> selectRootByProductId(@Param("productId") Long productId,
                                               @Param("ratingFilter") String ratingFilter,
                                               @Param("hasImage") Boolean hasImage,
                                               @Param("sort") String sort);

    /** 追加评价 */
    List<ProductReview> selectByParentId(Long parentId);

    int countByProductId(Long productId);

    /** 评分统计 */
    List<java.util.Map<String, Object>> selectRatingStats(Long productId);

    /** 用户的所有评价 */
    List<ProductReview> selectByUserId(Long userId);
}
