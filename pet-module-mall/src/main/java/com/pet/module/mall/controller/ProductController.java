package com.pet.module.mall.controller;

import com.pet.common.result.Result;
import com.pet.module.mall.model.entity.MallCategory;
import com.pet.module.mall.model.vo.ProductDetailVo;
import com.pet.module.mall.model.vo.ProductListVo;
import com.pet.module.mall.service.CategoryService;
import com.pet.module.mall.service.ProductService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mall")
public class ProductController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    /**
     * 商品分类列表
     */
    @GetMapping("/categories")
    public Result<List<MallCategory>> categories() {
        return Result.success(categoryService.getCategoryList());
    }

    /**
     * 商品列表（分页+分类筛选）
     */
    @GetMapping("/products")
    public Result<PageInfo<ProductListVo>> products(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ProductListVo> list = productService.getProductList(categoryId, page, size);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 商品详情
     */
    @GetMapping("/products/{id}")
    public Result<ProductDetailVo> detail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }
}