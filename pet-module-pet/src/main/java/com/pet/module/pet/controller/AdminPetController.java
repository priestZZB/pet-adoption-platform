package com.pet.module.pet.controller;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.framework.annotation.RequireRole;
import com.pet.module.pet.model.dto.PetReviewDto;
import com.pet.module.pet.model.vo.PetCategoryVo;
import com.pet.module.pet.model.vo.PetDetailVo;
import com.pet.module.pet.model.vo.PetListVo;
import com.pet.module.pet.service.PetCategoryService;
import com.pet.module.pet.service.PetReviewService;
import com.pet.module.pet.service.PetService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Map;

@Log("宠物管理")
@Api(tags = "管理员-宠物管理")
@RestController
@RequestMapping("/api/admin")
@RequireRole("ADMIN")
public class AdminPetController {

    @Autowired
    private PetCategoryService petCategoryService;

    @Autowired
    private PetService petService;

    @Autowired
    private PetReviewService petReviewService;

    /**
     * 新增宠物分类
     */
    @ApiOperation("新增宠物分类")
    @PostMapping("/pet/categories")
    public Result<String> addCategory(@RequestParam String name,
                                      @RequestParam(defaultValue = "0") Integer sortOrder) {
        petCategoryService.add(name, sortOrder);
        return Result.success("新增成功");
    }

    /**
     * 编辑宠物分类
     */
    @ApiOperation("编辑宠物分类")
    @PutMapping("/pet/categories/{id}")
    public Result<String> updateCategory(@PathVariable Long id,
                                         @RequestParam String name,
                                         @RequestParam(defaultValue = "0") Integer sortOrder) {
        petCategoryService.update(id, name, sortOrder);
        return Result.success("修改成功");
    }

    /**
     * 删除宠物分类
     */
    @ApiOperation("删除宠物分类")
    @DeleteMapping("/pet/categories/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        petCategoryService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 所有宠物列表（管理员）
     */
    @ApiOperation("所有宠物列表（管理员）")
    @GetMapping("/pets")
    public Result<PageInfo<PetListVo>> allPets(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<PetListVo> list = petService.getPetList(categoryId, keyword, status, page, size);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 上架/下架/删除宠物
     */
    @ApiOperation("上架/下架/删除宠物")
    @PutMapping("/pets/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam String status) {
        petService.adminUpdateStatus(id, status);
        return Result.success("状态已更新");
    }

    /**
     * 批量上架/下架宠物
     */
    @ApiOperation("批量上架/下架宠物")
    @PostMapping("/pets/batch-status")
    public Result<String> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        List<Long> ids = extractLongList(body, "ids");
        String status = (String) body.get("status");
        if (status == null || status.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "缺少参数: status");
        }
        petService.batchUpdateStatus(ids, status);
        return Result.success("批量操作成功");
    }

    /**
     * 批量软删除宠物
     */
    @ApiOperation("批量软删除宠物")
    @PostMapping("/pets/batch-delete")
    public Result<String> batchDelete(@RequestBody Map<String, Object> body) {
        List<Long> ids = extractLongList(body, "ids");
        petService.batchDelete(ids);
        return Result.success("批量删除成功");
    }

    private List<Long> extractLongList(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "缺少参数: " + key);
        }
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .map(item -> item instanceof Number ? ((Number) item).longValue() : Long.valueOf(item.toString()))
                    .collect(Collectors.toList());
        }
        throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "参数格式错误，应为数组: " + key);
    }

    /**
     * 终审（通过/打回）
     */
    @ApiOperation("终审（通过/打回）")
    @PostMapping("/pets/{id}/final-review")
    public Result<String> finalReview(HttpServletRequest request,
                                      @PathVariable Long id,
                                      @RequestBody PetReviewDto dto) {
        Long adminId = Long.valueOf(request.getAttribute("userId").toString());
        petReviewService.finalReview(adminId, id, dto);
        return Result.success("终审完成");
    }
}