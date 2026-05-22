package com.pet.module.mall.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.module.mall.model.dto.ReviewDto;
import com.pet.module.mall.model.vo.ReviewVo;
import com.pet.module.mall.service.ProductReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Log("商品评价")
@Api(tags = "商城-评价")
@RestController
@RequestMapping("/api/mall")
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    @ApiOperation("商品评价列表")
    @GetMapping("/products/{productId}/reviews")
    public Result<Map<String, Object>> list(@PathVariable Long productId,
                                             @RequestParam(required = false) String rating,
                                             @RequestParam(required = false) Boolean hasImage,
                                             @RequestParam(defaultValue = "latest") String sort,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.success(productReviewService.getReviews(productId, rating, hasImage, sort, page, size));
    }

    @ApiOperation("商品评价统计")
    @GetMapping("/products/{productId}/review-stats")
    public Result<Map<String, Object>> stats(@PathVariable Long productId) {
        return Result.success(productReviewService.getReviewStats(productId));
    }

    @ApiOperation("提交评价")
    @PostMapping("/reviews")
    public Result<ReviewVo> add(@RequestBody ReviewDto dto, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(productReviewService.addReview(userId, dto));
    }

    @ApiOperation("追加评价")
    @PostMapping("/reviews/{reviewId}/additional")
    public Result<ReviewVo> additional(@PathVariable Long reviewId,
                                        @RequestBody ReviewDto dto,
                                        HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(productReviewService.addAdditional(reviewId, userId, dto));
    }

    @ApiOperation("我的评价列表")
    @GetMapping("/reviews/my")
    public Result<List<ReviewVo>> myReviews(HttpServletRequest request,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        List<ReviewVo> list = productReviewService.getUserReviews(userId, page, size);
        return Result.success(list);
    }
}
