package com.pet.module.mall.model.vo;

import java.math.BigDecimal;

/**
 * 商品列表返回
 */
public class ProductListVo {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String image;
    private Integer stock;
    private String categoryName;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}