package com.pet.module.mall.service;

import com.pet.module.mall.model.vo.CartVo;
import java.util.List;

public interface CartService {

    void add(Long userId, Long productId, Integer quantity);

    List<CartVo> getCart(Long userId);

    void updateQuantity(Long userId, Long productId, Integer quantity);

    void remove(Long userId, Long productId);

    void clear(Long userId);
}