package com.pet.module.mall.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.mall.model.dto.ShipDto;
import com.pet.module.mall.model.vo.OrderVo;
import com.pet.module.mall.service.CategoryService;
import com.pet.module.mall.service.OrderService;
import com.pet.module.mall.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Api(tags = "管理员-商城管理")
@RestController
@RequestMapping("/api/admin/mall")
@RequireRole("ADMIN")
public class AdminMallController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    /**
     * 新增商品分类
     */
    @ApiOperation("新增商品分类")
    @PostMapping("/categories")
    public Result<String> addCategory(@RequestParam String name,
                                      @RequestParam(defaultValue = "0") Integer sortOrder) {
        categoryService.add(name, sortOrder);
        return Result.success("新增成功");
    }

    /**
     * 编辑商品分类
     */
    @ApiOperation("编辑商品分类")
    @PutMapping("/categories/{id}")
    public Result<String> updateCategory(@PathVariable Long id,
                                         @RequestParam String name,
                                         @RequestParam(defaultValue = "0") Integer sortOrder) {
        categoryService.update(id, name, sortOrder);
        return Result.success("修改成功");
    }

    /**
     * 删除商品分类
     */
    @ApiOperation("删除商品分类")
    @DeleteMapping("/categories/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 新增商品
     */
    @ApiOperation("新增商品")
    @PostMapping("/products")
    public Result<String> addProduct(@RequestParam Long categoryId,
                                     @RequestParam String name,
                                     @RequestParam(required = false) String description,
                                     @RequestParam BigDecimal price,
                                     @RequestParam(defaultValue = "0") Integer stock,
                                     @RequestParam(required = false) String image) {
        productService.add(categoryId, name, description, price, stock, image);
        return Result.success("新增成功");
    }

    /**
     * 编辑商品
     */
    @ApiOperation("编辑商品")
    @PutMapping("/products/{id}")
    public Result<String> updateProduct(@PathVariable Long id,
                                        @RequestParam Long categoryId,
                                        @RequestParam String name,
                                        @RequestParam(required = false) String description,
                                        @RequestParam BigDecimal price,
                                        @RequestParam(defaultValue = "0") Integer stock,
                                        @RequestParam(required = false) String image) {
        productService.update(id, categoryId, name, description, price, stock, image);
        return Result.success("修改成功");
    }

    /**
     * 上架/下架商品
     */
    @ApiOperation("上架/下架商品")
    @PutMapping("/products/{id}/status")
    public Result<String> toggleStatus(@PathVariable Long id) {
        productService.toggleStatus(id);
        return Result.success("状态已更新");
    }

    /**
     * 所有订单列表
     */
    @ApiOperation("所有订单列表（管理员）")
    @GetMapping("/orders")
    public Result<List<OrderVo>> orders(@RequestParam(required = false) String status) {
        return Result.success(orderService.getAllOrders(status));
    }

    /**
     * 订单详情
     */
    @ApiOperation("订单详情（管理员）")
    @GetMapping("/orders/{id}")
    public Result<OrderVo> orderDetail(@PathVariable Long id) {
        return Result.success(orderService.getAdminOrderDetail(id));
    }

    /**
     * 发货（填物流单号）
     */
    @ApiOperation("发货")
    @PutMapping("/orders/{id}/ship")
    public Result<String> ship(@PathVariable Long id, @RequestBody ShipDto dto) {
        orderService.ship(id, dto);
        return Result.success("发货成功");
    }
}