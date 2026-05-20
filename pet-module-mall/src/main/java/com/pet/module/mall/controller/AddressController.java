package com.pet.module.mall.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.module.mall.model.dto.AddressDto;
import com.pet.module.mall.model.vo.AddressVo;
import com.pet.module.mall.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Log("收货地址")
@Api(tags = "商城-收货地址")
@RestController
@RequestMapping("/api/mall/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @ApiOperation("收货地址列表")
    @GetMapping
    public Result<List<AddressVo>> list(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(addressService.getUserAddresses(userId));
    }

    @ApiOperation("新增收货地址")
    @PostMapping
    public Result<AddressVo> add(HttpServletRequest request, @RequestBody AddressDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(addressService.add(userId, dto));
    }

    @ApiOperation("编辑收货地址")
    @PutMapping("/{id}")
    public Result<AddressVo> update(HttpServletRequest request, @PathVariable Long id, @RequestBody AddressDto dto) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(addressService.update(userId, id, dto));
    }

    @ApiOperation("删除收货地址")
    @DeleteMapping("/{id}")
    public Result<String> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        addressService.delete(userId, id);
        return Result.success("删除成功");
    }

    @ApiOperation("设为默认地址")
    @PutMapping("/{id}/default")
    public Result<String> setDefault(HttpServletRequest request, @PathVariable Long id) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        addressService.setDefault(userId, id);
        return Result.success("已设为默认地址");
    }

    @ApiOperation("获取默认地址")
    @GetMapping("/default")
    public Result<AddressVo> getDefault(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(addressService.getDefault(userId));
    }
}
