package com.pet.module.mall.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.module.mall.model.dto.CheckoutDto;
import com.pet.module.mall.model.vo.OrderVo;
import com.pet.module.mall.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Log("商城订单")
@Api(tags = "商城-订单（用户）")
@RestController
@RequestMapping("/api/mall/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单（从购物车结算）
     */
    @ApiOperation("创建订单")
    @PostMapping
    public Result<OrderVo> create(HttpServletRequest request, @RequestBody CheckoutDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(orderService.create(userId, dto));
    }

    /**
     * 我的订单列表
     */
    @ApiOperation("我的订单列表")
    @GetMapping
    public Result<List<OrderVo>> list(HttpServletRequest request,
                                      @RequestParam(required = false) String status) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(orderService.getUserOrders(userId, status));
    }

    /**
     * 订单详情
     */
    @ApiOperation("订单详情")
    @GetMapping("/{id}")
    public Result<OrderVo> detail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    /**
     * 模拟支付确认
     */
    @ApiOperation("模拟支付")
    @PostMapping("/{id}/pay")
    public Result<String> pay(@PathVariable Long id) {
        orderService.pay(id);
        return Result.success("支付成功");
    }

    /**
     * 确认收货
     */
    @ApiOperation("确认收货")
    @PutMapping("/{id}/receive")
    public Result<String> receive(@PathVariable Long id) {
        orderService.receive(id);
        return Result.success("已确认收货");
    }

    /**
     * 取消订单
     */
    @ApiOperation("取消订单")
    @PutMapping("/{id}/cancel")
    public Result<String> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success("已取消");
    }
}