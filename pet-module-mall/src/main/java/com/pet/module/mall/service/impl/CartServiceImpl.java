package com.pet.module.mall.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.mall.mapper.MallCartMapper;
import com.pet.module.mall.mapper.MallProductMapper;
import com.pet.module.mall.model.entity.MallCart;
import com.pet.module.mall.model.entity.MallProduct;
import com.pet.module.mall.model.vo.CartVo;
import com.pet.module.mall.service.CartService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate redisTemplate;

    private final MallProductMapper mallProductMapper;

    private final MallCartMapper mallCartMapper;

    public CartServiceImpl(
                StringRedisTemplate redisTemplate,
            MallProductMapper mallProductMapper,
            MallCartMapper mallCartMapper) {
        this.redisTemplate = redisTemplate;
        this.mallProductMapper = mallProductMapper;
        this.mallCartMapper = mallCartMapper;
    }

    private String cartKey(Long userId) {
        return "mall:cart:" + userId;
    }

    @Override
    @Transactional
    public void add(Long userId, Long productId, Integer quantity) {
        MallProduct product = mallProductMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCodeEnum.PRODUCT_NOT_FOUND);
        }
        // Redis
        String key = cartKey(userId);
        String field = String.valueOf(productId);
        String val = (String) redisTemplate.opsForHash().get(key, field);
        int newQty = quantity;
        if (val != null) {
            newQty += Integer.parseInt(val);
        }
        redisTemplate.opsForHash().put(key, field, String.valueOf(newQty));

        // MySQL（价格快照：加入时的价格）
        MallCart cart = new MallCart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setPrice(product.getPrice());
        mallCartMapper.upsert(cart);
    }

    @Override
    public List<CartVo> getCart(Long userId) {
        String key = cartKey(userId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        // Redis 有数据，直接读 Redis
        if (!entries.isEmpty()) {
            return buildCartVoList(entries);
        }

        // Redis 为空（重启/过期），从 MySQL 恢复
        List<MallCart> dbCarts = mallCartMapper.selectByUserId(userId);
        if (dbCarts.isEmpty()) {
            return new ArrayList<>();
        }
        // 写回 Redis 以便下次快速读取
        for (MallCart db : dbCarts) {
            redisTemplate.opsForHash().put(key, String.valueOf(db.getProductId()), String.valueOf(db.getQuantity()));
        }

        // 从 MySQL 数据构建返回
        Map<Object, Object> restored = redisTemplate.opsForHash().entries(key);
        return buildCartVoList(restored);
    }

    @Override
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        String key = cartKey(userId);
        String field = String.valueOf(productId);
        if (redisTemplate.opsForHash().hasKey(key, field)) {
            redisTemplate.opsForHash().put(key, field, String.valueOf(quantity));
        }

        // 同步更新 MySQL（只在quantity>0时）
        if (quantity > 0) {
            MallProduct product = mallProductMapper.selectById(productId);
            if (product != null) {
                MallCart cart = new MallCart();
                cart.setUserId(userId);
                cart.setProductId(productId);
                cart.setQuantity(0);
                cart.setPrice(product.getPrice());
                // 先删再插，实现覆写数量
                mallCartMapper.deleteByUserAndProduct(userId, productId);
                cart.setQuantity(quantity);
                mallCartMapper.upsert(cart);
            }
        }
    }

    @Override
    public void remove(Long userId, Long productId) {
        String key = cartKey(userId);
        redisTemplate.opsForHash().delete(key, String.valueOf(productId));

        mallCartMapper.deleteByUserAndProduct(userId, productId);
    }

    @Override
    public void clear(Long userId) {
        redisTemplate.delete(cartKey(userId));

        mallCartMapper.deleteByUserId(userId);
    }

    /**
     * 从 Redis Hash entries 构建 CartVo 列表
     */
    private List<CartVo> buildCartVoList(Map<Object, Object> entries) {
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
}
