package com.pet.module.mall.service;

import com.pet.module.mall.model.vo.ProductDetailVo;
import com.pet.module.mall.model.vo.ProductListVo;
import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    List<ProductListVo> getProductList(Long categoryId, int page, int size);

    ProductDetailVo getProductDetail(Long id);

    Long add(Long categoryId, String name, String description, BigDecimal price, Integer stock, String image);

    void update(Long id, Long categoryId, String name, String description, BigDecimal price, Integer stock, String image);

    void toggleStatus(Long id);
}