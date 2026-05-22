package com.pet.module.mall.service;

import com.pet.module.mall.model.dto.ReviewDto;
import com.pet.module.mall.model.vo.ReviewVo;

import java.util.List;
import java.util.Map;

public interface ProductReviewService {

    ReviewVo addReview(Long userId, ReviewDto dto);

    ReviewVo addAdditional(Long reviewId, Long userId, ReviewDto dto);

    Map<String, Object> getReviews(Long productId, String ratingFilter, Boolean hasImage, String sort, int page, int size);

    Map<String, Object> getReviewStats(Long productId);

    List<ReviewVo> getUserReviews(Long userId, int page, int size);
}
