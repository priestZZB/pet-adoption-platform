package com.pet.module.mall.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.mall.mapper.MallProductMapper;
import com.pet.module.mall.model.entity.MallProduct;
import com.pet.module.mall.model.vo.CartVo;
import com.pet.module.mall.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MallProductMapper mallProductMapper;

    private String cartKey(Long userId) {
        return "mall:cart:" + userId;
    }

    @Override
    public void add(Long userId, Long productId, Integer quantity) {
        MallProduct product = mallProductMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCodeEnum.PRODUCT_NOT_FOUND);
        }
        String key = cartKey(userId);
        String field = String.valueOf(productId);
        String val = (String) redisTemplate.opsForHash().get(key, field);
        int newQty = quantity;
        if (val != null) {
            newQty += Integer.parseInt(val);
        }
        redisTemplate.opsForHash().put(key, field, String.valueOf(newQty));
    }

    @Override
    public List<CartVo> getCart(Long userId) {
        String key = cartKey(userId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<CartVo> list = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long productId = Long.valueOf(entry.getKey().toString());
            int quantity = Integer.parseInt(entry.getValue().toString());
            MallProduct product = mallProductMapper.selectById(productId);
            if (product == null) continue;
            CartVo vo = new CartVo();
            vo.setProductId(productId);
            vo.setProductName(product.getName());
            vo.setProductImage(product.getImage());
            vo.setPrice(product.getPrice());
            vo.setQuantity(quantity);
            vo.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            list.add(vo);
        }
        return list;
    }

    @Override
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        String key = cartKey(userId);
        String field = String.valueOf(productId);
        if (redisTemplate.opsForHash().hasKey(key, field)) {
            redisTemplate.opsForHash().put(key, field, String.valueOf(quantity));
        }
    }

    @Override
    public void remove(Long userId, Long productId) {
        String key = cartKey(userId);
        redisTemplate.opsForHash().delete(key, String.valueOf(productId));
    }

    @Override
    public void clear(Long userId) {
        redisTemplate.delete(cartKey(userId));
    }
}