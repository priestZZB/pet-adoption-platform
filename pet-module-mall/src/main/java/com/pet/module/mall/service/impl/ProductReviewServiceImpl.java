package com.pet.module.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.mall.mapper.MallOrderItemMapper;
import com.pet.module.mall.mapper.MallOrderMapper;
import com.pet.module.mall.mapper.MallProductMapper;
import com.pet.module.mall.mapper.ProductReviewMapper;
import com.pet.module.mall.model.dto.ReviewDto;
import com.pet.module.mall.model.entity.MallOrder;
import com.pet.module.mall.model.entity.MallOrderItem;
import com.pet.module.mall.model.entity.MallProduct;
import com.pet.module.mall.model.entity.ProductReview;
import com.pet.module.mall.model.vo.ReviewVo;
import com.pet.module.mall.service.ProductReviewService;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.system.model.entity.SysUser;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewMapper reviewMapper;

    private final MallProductMapper productMapper;

    private final MallOrderItemMapper orderItemMapper;

    private final UserMapper userMapper;

    public ProductReviewServiceImpl(
            ProductReviewMapper reviewMapper,
            MallProductMapper productMapper,
            MallOrderItemMapper orderItemMapper,
            UserMapper userMapper) {
        this.reviewMapper = reviewMapper;
        this.productMapper = productMapper;
        this.orderItemMapper = orderItemMapper;
        this.userMapper = userMapper;
    }


    @Override
    @Transactional
    public ReviewVo addReview(Long userId, ReviewDto dto) {
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "评分需1-5星");
        }

        MallOrderItem item = orderItemMapper.selectById(dto.getOrderItemId());
        if (item == null) throw new BusinessException(ResultCodeEnum.ORDER_NOT_FOUND, "订单项不存在");

        ProductReview existing = reviewMapper.selectByOrderItemId(dto.getOrderItemId());
        if (existing != null) throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "该商品已评价");

        ProductReview review = new ProductReview();
        review.setProductId(item.getProductId());
        review.setOrderItemId(dto.getOrderItemId());
        review.setUserId(userId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 0);
        review.setIsAdditional(0);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            review.setImages(String.join(",", dto.getImages()));
        }
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            review.setTags(String.join(",", dto.getTags()));
        }

        reviewMapper.insert(review);
        return toVo(review);
    }

    @Override
    @Transactional
    public ReviewVo addAdditional(Long reviewId, Long userId, ReviewDto dto) {
        ProductReview parent = reviewMapper.selectById(reviewId);
        if (parent == null) throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "原始评价不存在");

        ProductReview additional = new ProductReview();
        additional.setProductId(parent.getProductId());
        additional.setOrderItemId(parent.getOrderItemId());
        additional.setUserId(userId);
        additional.setRating(parent.getRating());
        additional.setContent(dto.getContent());
        additional.setParentId(reviewId);
        additional.setIsAdditional(1);
        additional.setStatus(1);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            additional.setImages(String.join(",", dto.getImages()));
        }

        reviewMapper.insert(additional);
        return toVo(additional);
    }

    @Override
    public Map<String, Object> getReviews(Long productId, String ratingFilter, Boolean hasImage, String sort, int page, int size) {
        PageHelper.startPage(page, size);
        List<ProductReview> roots = reviewMapper.selectRootByProductId(productId, ratingFilter, hasImage, sort);
        int total = reviewMapper.countByProductId(productId);

        List<ReviewVo> list = roots.stream().map(r -> {
            ReviewVo vo = toVo(r);
            List<ProductReview> additions = reviewMapper.selectByParentId(r.getId());
            if (!additions.isEmpty()) {
                vo.setAdditions(additions.stream().map(this::toVo).collect(Collectors.toList()));
            }
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    public Map<String, Object> getReviewStats(Long productId) {
        List<Map<String, Object>> raw = reviewMapper.selectRatingStats(productId);

        int total = 0;
        double sum = 0;
        Map<Integer, Integer> dist = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) dist.put(i, 0);

        for (Map<String, Object> row : raw) {
            int rating = ((Number) row.get("rating")).intValue();
            int count = ((Number) row.get("count")).intValue();
            dist.put(rating, count);
            total += count;
            sum += rating * count;
        }

        double avg = total > 0 ? Math.round(sum / total * 10.0) / 10.0 : 0.0;
        int goodCount = dist.get(5) + dist.get(4);
        int goodRate = total > 0 ? (int) Math.round(goodCount * 100.0 / total) : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("average", avg);
        stats.put("total", total);
        stats.put("goodRate", goodRate);
        stats.put("distribution", dist);
        return stats;
    }

    @Override
    public List<ReviewVo> getUserReviews(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<ProductReview> list = reviewMapper.selectByUserId(userId);
        return list.stream().map(this::toVo).collect(Collectors.toList());
    }

    private ReviewVo toVo(ProductReview review) {
        ReviewVo vo = new ReviewVo();
        BeanUtils.copyProperties(review, vo);

        if (review.getImages() != null && !review.getImages().isEmpty()) {
            vo.setImages(Arrays.asList(review.getImages().split(",")));
        }
        if (review.getTags() != null && !review.getTags().isEmpty()) {
            vo.setTags(Arrays.asList(review.getTags().split(",")));
        }

        if (review.getIsAnonymous() != 1) {
            SysUser user = userMapper.selectById(review.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                vo.setAvatar(user.getAvatar());
            }
        } else {
            vo.setNickname("匿名用户");
        }

        return vo;
    }
}
