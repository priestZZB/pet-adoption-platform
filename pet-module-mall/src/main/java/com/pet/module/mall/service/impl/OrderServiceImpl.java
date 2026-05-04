package com.pet.module.mall.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.common.util.IdGenerator;
import com.pet.module.mall.mapper.MallOrderItemMapper;
import com.pet.module.mall.mapper.MallOrderMapper;
import com.pet.module.mall.mapper.MallProductMapper;
import com.pet.module.mall.model.dto.CheckoutDto;
import com.pet.module.mall.model.dto.ShipDto;
import com.pet.module.mall.model.entity.MallOrder;
import com.pet.module.mall.model.entity.MallOrderItem;
import com.pet.module.mall.model.entity.MallProduct;
import com.pet.module.mall.model.vo.CartVo;
import com.pet.module.mall.model.vo.OrderVo;
import com.pet.module.mall.service.CartService;
import com.pet.module.mall.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private MallOrderMapper mallOrderMapper;

    @Autowired
    private MallOrderItemMapper mallOrderItemMapper;

    @Autowired
    private MallProductMapper mallProductMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public OrderVo create(Long userId, CheckoutDto dto) {
        // 从购物车获取商品
        List<CartVo> cart = cartService.getCart(userId);
        if (cart.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "购物车为空");
        }

        // 计算总金额
        BigDecimal total = BigDecimal.ZERO;
        List<MallOrderItem> items = new ArrayList<>();
        for (CartVo item : cart) {
            MallProduct product = mallProductMapper.selectById(item.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException(ResultCodeEnum.PRODUCT_NOT_FOUND, "商品已下架: " + item.getProductName());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "库存不足: " + product.getName());
            }
            total = total.add(item.getSubtotal());

            MallOrderItem orderItem = new MallOrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImage());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());
            items.add(orderItem);
        }

        // 创建订单
        MallOrder order = new MallOrder();
        order.setOrderNo(IdGenerator.generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus("PENDING_PAY");
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        mallOrderMapper.insert(order);

        // 保存订单项
        for (MallOrderItem item : items) {
            item.setOrderId(order.getId());
        }
        mallOrderItemMapper.insertBatch(items);

        // 扣减库存
        for (CartVo item : cart) {
            MallProduct product = mallProductMapper.selectById(item.getProductId());
            MallProduct update = new MallProduct();
            update.setId(product.getId());
            update.setStock(product.getStock() - item.getQuantity());
            mallProductMapper.updateById(update);
        }

        // 清空购物车
        redisTemplate.delete("mall:cart:" + userId);

        return convertToVo(order);
    }

    @Override
    public List<OrderVo> getUserOrders(Long userId, String status) {
        List<MallOrder> orders = mallOrderMapper.selectByUserId(userId, status);
        return orders.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public OrderVo getOrderDetail(Long id) {
        MallOrder order = mallOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCodeEnum.ORDER_NOT_FOUND);
        }
        return convertToVo(order);
    }

    @Override
    @Transactional
    public void pay(Long id) {
        MallOrder order = mallOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCodeEnum.ORDER_NOT_FOUND);
        }
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "订单状态不支持支付");
        }
        MallOrder update = new MallOrder();
        update.setId(id);
        update.setStatus("PAID");
        mallOrderMapper.updateById(update);
    }

    @Override
    @Transactional
    public void receive(Long id) {
        MallOrder order = mallOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCodeEnum.ORDER_NOT_FOUND);
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "订单状态不支持收货");
        }
        MallOrder update = new MallOrder();
        update.setId(id);
        update.setStatus("RECEIVED");
        mallOrderMapper.updateById(update);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        MallOrder order = mallOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCodeEnum.ORDER_NOT_FOUND);
        }
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "仅待支付订单可取消");
        }
        MallOrder update = new MallOrder();
        update.setId(id);
        update.setStatus("CANCELLED");
        mallOrderMapper.updateById(update);

        // 恢复库存
        List<MallOrderItem> items = mallOrderItemMapper.selectByOrderId(id);
        for (MallOrderItem item : items) {
            MallProduct product = mallProductMapper.selectById(item.getProductId());
            if (product != null) {
                MallProduct prodUpdate = new MallProduct();
                prodUpdate.setId(product.getId());
                prodUpdate.setStock(product.getStock() + item.getQuantity());
                mallProductMapper.updateById(prodUpdate);
            }
        }
    }

    @Override
    public List<OrderVo> getAllOrders(String status) {
        List<MallOrder> orders = mallOrderMapper.selectAll(status);
        return orders.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public OrderVo getAdminOrderDetail(Long id) {
        return getOrderDetail(id);
    }

    @Override
    @Transactional
    public void ship(Long id, ShipDto dto) {
        MallOrder order = mallOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCodeEnum.ORDER_NOT_FOUND);
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "仅已支付订单可发货");
        }
        MallOrder update = new MallOrder();
        update.setId(id);
        update.setStatus("SHIPPED");
        update.setLogisticsNo(dto.getLogisticsNo());
        update.setLogisticsStatus(dto.getLogisticsStatus());
        mallOrderMapper.updateById(update);
    }

    private OrderVo convertToVo(MallOrder order) {
        OrderVo vo = new OrderVo();
        BeanUtils.copyProperties(order, vo);

        List<MallOrderItem> items = mallOrderItemMapper.selectByOrderId(order.getId());
        List<OrderVo.OrderItemVo> itemVos = items.stream().map(item -> {
            OrderVo.OrderItemVo itemVo = new OrderVo.OrderItemVo();
            BeanUtils.copyProperties(item, itemVo);
            return itemVo;
        }).collect(Collectors.toList());
        vo.setItems(itemVos);

        return vo;
    }
}