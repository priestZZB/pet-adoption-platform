package com.pet.module.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.mall.mapper.MallCategoryMapper;
import com.pet.module.mall.mapper.MallProductMapper;
import com.pet.module.mall.model.entity.MallCategory;
import com.pet.module.mall.model.entity.MallProduct;
import com.pet.module.mall.model.vo.ProductDetailVo;
import com.pet.module.mall.model.vo.ProductListVo;
import com.pet.module.mall.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "product")
public class ProductServiceImpl implements ProductService {

    @Autowired
    private MallProductMapper mallProductMapper;

    @Autowired
    private MallCategoryMapper mallCategoryMapper;

    @Cacheable(key = "'list:' + #categoryId")
    @Override
    public List<ProductListVo> getProductList(Long categoryId, int page, int size) {
        PageHelper.startPage(page, size);
        List<MallProduct> list = mallProductMapper.selectPage(categoryId);
        return list.stream().map(this::convertToListVo).collect(Collectors.toList());
    }

    @Cacheable(key = "'detail:' + #id")
    @Override
    public ProductDetailVo getProductDetail(Long id) {
        MallProduct product = mallProductMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCodeEnum.PRODUCT_NOT_FOUND);
        }
        return convertToDetailVo(product);
    }

    @CacheEvict(allEntries = true)
    @Override
    public Long add(Long categoryId, String name, String description, BigDecimal price, Integer stock, String image) {
        MallCategory category = mallCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCodeEnum.CATEGORY_NOT_FOUND);
        }
        MallProduct product = new MallProduct();
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setImage(image);
        product.setStatus(1);
        mallProductMapper.insert(product);
        return product.getId();
    }

    @CacheEvict(allEntries = true)
    @Override
    public void update(Long id, Long categoryId, String name, String description, BigDecimal price, Integer stock, String image) {
        MallProduct product = mallProductMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCodeEnum.PRODUCT_NOT_FOUND);
        }
        MallProduct update = new MallProduct();
        update.setId(id);
        update.setCategoryId(categoryId);
        update.setName(name);
        update.setDescription(description);
        update.setPrice(price);
        update.setStock(stock);
        update.setImage(image);
        mallProductMapper.updateById(update);
    }
    @CacheEvict(allEntries = true)
    @Override
    public void evictProductCache() {
        // 清空商品缓存（下单扣减库存后调用）
    }

    @CacheEvict(allEntries = true)
    @Override
    public void toggleStatus(Long id) {
        MallProduct product = mallProductMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCodeEnum.PRODUCT_NOT_FOUND);
        }
        MallProduct update = new MallProduct();
        update.setId(id);
        update.setStatus(product.getStatus() == 0 ? 1 : 0);
        mallProductMapper.updateById(update);
    }

    private ProductListVo convertToListVo(MallProduct product) {
        ProductListVo vo = new ProductListVo();
        BeanUtils.copyProperties(product, vo);
        MallCategory category = mallCategoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        return vo;
    }

    private ProductDetailVo convertToDetailVo(MallProduct product) {
        ProductDetailVo vo = new ProductDetailVo();
        BeanUtils.copyProperties(product, vo);
        MallCategory category = mallCategoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        return vo;
    }
}