package com.pet.module.mall.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品评价表 product_review
 */
public class ProductReview {

    private Long id;
    private Long productId;
    private Long orderItemId;
    private Long userId;
    private Integer rating;
    private String content;
    private String images;
    private String tags;
    private Long parentId;
    private Integer isAdditional;
    private Integer isAnonymous;
    private Integer status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getIsAdditional() { return isAdditional; }
    public void setIsAdditional(Integer isAdditional) { this.isAdditional = isAdditional; }
    public Integer getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Integer isAnonymous) { this.isAnonymous = isAnonymous; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
