package com.pet.module.mall.service;

import com.pet.module.mall.model.dto.CheckoutDto;
import com.pet.module.mall.model.dto.ShipDto;
import com.pet.module.mall.model.vo.OrderVo;
import java.util.List;

public interface OrderService {

    OrderVo create(Long userId, CheckoutDto dto);

    /**
     * 立即购买（直接传入商品ID和数量，不走购物车）
     */
    OrderVo buyNow(Long userId, Long productId, Integer quantity);

    List<OrderVo> getUserOrders(Long userId, String status);

    OrderVo getOrderDetail(Long id);

    void pay(Long id);

    void receive(Long id);

    void cancel(Long id);

    List<OrderVo> getAllOrders(String status, int page, int size);

    OrderVo getAdminOrderDetail(Long id);

    void ship(Long id, ShipDto dto);
}