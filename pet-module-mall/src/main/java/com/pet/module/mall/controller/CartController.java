package com.pet.module.mall.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.module.mall.model.dto.CartAddDto;
import com.pet.module.mall.model.vo.CartVo;
import com.pet.module.mall.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Log("购物车")
@Api(tags = "商城-购物车")
@RestController
@RequestMapping("/api/mall/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 加入购物车
     */
    @ApiOperation("加入购物车")
    @PostMapping
    public Result<String> add(HttpServletRequest request, @RequestBody CartAddDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        cartService.add(userId, dto.getProductId(), dto.getQuantity());
        return Result.success("已加入购物车");
    }

    /**
     * 购物车列表
     */
    @ApiOperation("购物车列表")
    @GetMapping
    public Result<List<CartVo>> list(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(cartService.getCart(userId));
    }

    /**
     * 修改商品数量
     */
    @ApiOperation("修改商品数量")
    @PutMapping("/{id}")
    public Result<String> update(HttpServletRequest request,
                                 @PathVariable Long id,
                                 @RequestParam Integer quantity) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        cartService.updateQuantity(userId, id, quantity);
        return Result.success("修改成功");
    }

    /**
     * 删除购物车商品
     */
    @ApiOperation("删除购物车商品")
    @DeleteMapping("/{id}")
    public Result<String> remove(HttpServletRequest request, @PathVariable Long id) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        cartService.remove(userId, id);
        return Result.success("已删除");
    }

    /**
     * 清空购物车
     */
    @ApiOperation("清空购物车")
    @DeleteMapping
    public Result<String> clear(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        cartService.clear(userId);
        return Result.success("已清空");
    }
}