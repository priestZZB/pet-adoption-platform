package com.pet.module.mall.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.event.NotificationEvent;
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
import com.pet.module.mall.model.entity.ShippingAddress;
import com.pet.module.mall.mapper.ShippingAddressMapper;
import com.pet.module.mall.service.CartService;
import com.pet.module.mall.service.OrderService;
import com.pet.module.mall.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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
    private ShippingAddressMapper shippingAddressMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    private static final Map<String, String[]> COURIER_CONFIG = new LinkedHashMap<>();
    static {
        COURIER_CONFIG.put("顺丰快递", new String[]{"SF", "10"});
        COURIER_CONFIG.put("圆通快递", new String[]{"YT", "10"});
        COURIER_CONFIG.put("申通快递", new String[]{"STO", "9"});
        COURIER_CONFIG.put("中通快递", new String[]{"ZTO", "9"});
        COURIER_CONFIG.put("EMS", new String[]{"EMS", "10"});
        COURIER_CONFIG.put("京东快递", new String[]{"JD", "12"});
        COURIER_CONFIG.put("韵达快递", new String[]{"YD", "10"});
        COURIER_CONFIG.put("极兔快递", new String[]{"JT", "10"});
    }

    @Override
    @Transactional
    public OrderVo create(Long userId, CheckoutDto dto) {
        // 从购物车获取商品
        List<CartVo> cart = cartService.getCart(userId);
        if (cart.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "购物车为空");
        }

        // 如果传了 addressId，从收货地址表填充收货信息
        String receiverName = dto.getReceiverName();
        String receiverPhone = dto.getReceiverPhone();
        String receiverAddress = dto.getReceiverAddress();

        if (dto.getAddressId() != null) {
            ShippingAddress addr = shippingAddressMapper.selectById(dto.getAddressId());
            if (addr == null || !addr.getUserId().equals(userId)) {
                throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "收货地址不存在");
            }
            receiverName = addr.getReceiverName();
            receiverPhone = addr.getReceiverPhone();
            receiverAddress = addr.getReceiverAddress();
        }

        if (receiverName == null || receiverPhone == null || receiverAddress == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "请填写收货信息");
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
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
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

        // 清除商品缓存，库存变化即时显示
        productService.evictProductCache();

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
    public OrderVo buyNow(Long userId, Long productId, Integer quantity) {
        MallProduct product = mallProductMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException(ResultCodeEnum.PRODUCT_NOT_FOUND, "商品不存在或已下架");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "库存不足");
        }

        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(quantity));

        // 创建订单（收货信息暂空，用户在订单详情补充）
        MallOrder order = new MallOrder();
        order.setOrderNo(IdGenerator.generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus("PENDING_PAY");
        order.setReceiverName("待补充");
        order.setReceiverPhone("待补充");
        order.setReceiverAddress("待补充");
        mallOrderMapper.insert(order);

        // 保存订单项
        MallOrderItem item = new MallOrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getImage());
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());
        mallOrderItemMapper.insert(item);

        // 扣减库存
        MallProduct prodUpdate = new MallProduct();
        prodUpdate.setId(product.getId());
        prodUpdate.setStock(product.getStock() - quantity);
        mallProductMapper.updateById(prodUpdate);

        // 清除商品缓存
        productService.evictProductCache();

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

        eventPublisher.publishEvent(new NotificationEvent(
                order.getUserId(), "ORDER_STATUS",
                "订单支付成功",
                "订单" + order.getOrderNo() + "已支付成功，等待卖家发货",
                id));
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
        productService.evictProductCache();
    }

    @Override
    public List<OrderVo> getAllOrders(String status, int page, int size) {
        PageHelper.startPage(page, size);
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

        // 校验快递公司
        String company = dto.getCourierCompany();
        if (company == null || !COURIER_CONFIG.containsKey(company)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "请选择快递公司");
        }

        MallOrder update = new MallOrder();
        update.setId(id);
        update.setStatus("SHIPPED");
        update.setCourierCompany(company);

        // 生成随机物流时间线（秒，从发货时刻算起）
        Map<String, Integer> timeline = new HashMap<>();
        timeline.put("picked", 30 + random.nextInt(31));      // 30~60秒 → 已揽件
        timeline.put("transit", 60 + random.nextInt(61));     // 60~120秒 → 运输中
        timeline.put("delivering", 120 + random.nextInt(121));  // 120~240秒 → 派送中
        timeline.put("delivered", 240 + random.nextInt(121));   // 240~360秒 → 已送达
        try {
            update.setLogisticsTimeline(objectMapper.writeValueAsString(timeline));
        } catch (Exception e) {
            throw new BusinessException(ResultCodeEnum.UNKNOWN_ERROR, "生成物流时间线失败");
        }

        // 根据快递公司自动生成单号
        update.setLogisticsNo(generateTrackingNo(company));
        update.setLogisticsStatus("已发货");
        mallOrderMapper.updateById(update);

        eventPublisher.publishEvent(new NotificationEvent(
                order.getUserId(), "ORDER_STATUS",
                "订单已发货",
                "订单" + order.getOrderNo() + "已发货，快递：" + company + "，单号：" + update.getLogisticsNo(),
                id));
    }

    /**
     * 根据快递公司生成单号
     */
    private String generateTrackingNo(String company) {
        String[] config = COURIER_CONFIG.get(company);
        if (config == null) return null;
        String prefix = config[0];
        int digits = Integer.parseInt(config[1]);
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < digits; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 根据物流时间线实时计算当前物流状态
     */
    private String computeLogisticsStatus(MallOrder order) {
        if (order.getLogisticsTimeline() == null || order.getUpdatedAt() == null) {
            return order.getLogisticsStatus();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Integer> timeline = objectMapper.readValue(order.getLogisticsTimeline(), Map.class);
            long elapsed = Duration.between(order.getUpdatedAt(), LocalDateTime.now()).getSeconds();

            if (elapsed < timeline.get("picked"))     return "已发货";
            if (elapsed < timeline.get("transit"))    return "已揽件";
            if (elapsed < timeline.get("delivering")) return "运输中";
            if (elapsed < timeline.get("delivered"))  return "派送中";
            return "已送达";
        } catch (Exception e) {
            return order.getLogisticsStatus();
        }
    }

    private OrderVo convertToVo(MallOrder order) {
        OrderVo vo = new OrderVo();
        BeanUtils.copyProperties(order, vo);

        // 实时计算物流状态（覆盖 DB 中的静态值）
        if ("SHIPPED".equals(order.getStatus())) {
            vo.setLogisticsStatus(computeLogisticsStatus(order));
        }

        // 检查物流状态是否有进展，有则发送站内通知
        checkAndNotifyLogistics(order);

        List<MallOrderItem> items = mallOrderItemMapper.selectByOrderId(order.getId());
        List<OrderVo.OrderItemVo> itemVos = items.stream().map(item -> {
            OrderVo.OrderItemVo itemVo = new OrderVo.OrderItemVo();
            BeanUtils.copyProperties(item, itemVo);
            return itemVo;
        }).collect(Collectors.toList());
        vo.setItems(itemVos);

        return vo;
    }

    /**
     * 检查物流状态进展，状态变化时发送站内通知并更新DB记录
     * 物流顺序：已发货 → 已揽件 → 运输中 → 派送中 → 已送达
     */
    private static final List<String> LOGISTICS_ORDER = Arrays.asList("已发货", "已揽件", "运输中", "派送中", "已送达");

    private void checkAndNotifyLogistics(MallOrder order) {
        // 只有已发货状态需要检查
        if (!"SHIPPED".equals(order.getStatus())) return;
        // 无物流时间线则不处理
        if (order.getLogisticsTimeline() == null) return;

        String currentStatus = computeLogisticsStatus(order);
        String storedStatus = order.getLogisticsStatus();

        int currentIdx = LOGISTICS_ORDER.indexOf(currentStatus);
        int storedIdx = LOGISTICS_ORDER.indexOf(storedStatus);

        // 如果物流有进步（新的状态 > 旧的状态），发送通知并更新DB
        if (currentIdx > storedIdx) {
            MallOrder update = new MallOrder();
            update.setId(order.getId());
            update.setLogisticsStatus(currentStatus);
            mallOrderMapper.updateById(update);

            // 根据状态发通知
            String title = "物流更新";
            String content;
            switch (currentStatus) {
                case "已揽件":
                    content = "您的订单 " + order.getOrderNo() + " 已揽件，正在运往中转站";
                    break;
                case "运输中":
                    content = "您的订单 " + order.getOrderNo() + " 正在运输中，请耐心等待";
                    break;
                case "派送中":
                    content = "您的订单 " + order.getOrderNo() + " 正在派送中，即将送达";
                    break;
                case "已送达":
                    content = "您的订单 " + order.getOrderNo() + " 已送达，请及时确认收货";
                    break;
                default:
                    content = "您的订单 " + order.getOrderNo() + " 物流状态更新为：" + currentStatus;
            }
            eventPublisher.publishEvent(new NotificationEvent(
                    order.getUserId(), "ORDER_STATUS", title, content, order.getId()));
        }
    }
}
