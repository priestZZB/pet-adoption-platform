package com.pet.module.pet.controller;

import com.pet.common.result.Result;
import com.pet.framework.annotation.Log;
import com.pet.module.pet.model.dto.CommentDto;
import com.pet.module.pet.model.dto.ReplyDto;
import com.pet.module.pet.model.vo.CommentVo;
import com.pet.module.pet.service.PetCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Log("宠物评论")
@Api(tags = "宠物评论")
@RestController
@RequestMapping("/api/pets")
public class PetCommentController {

    @Autowired
    private PetCommentService petCommentService;

    @ApiOperation("获取宠物评论列表")
    @GetMapping("/{petId}/comments")
    public Result<Map<String, Object>> list(@PathVariable Long petId,
                                             @RequestParam(defaultValue = "latest") String sort,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             HttpServletRequest request) {
        Long userId = null;
        Object uid = request.getAttribute("userId");
        if (uid != null) userId = Long.valueOf(uid.toString());
        return Result.success(petCommentService.getComments(petId, userId, sort, page, size));
    }

    @ApiOperation("发表评论")
    @PostMapping("/{petId}/comments")
    public Result<CommentVo> add(@PathVariable Long petId,
                                  @RequestBody CommentDto dto,
                                  HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(petCommentService.addComment(petId, userId, dto));
    }

    @ApiOperation("回复评论")
    @PostMapping("/comments/{commentId}/reply")
    public Result<CommentVo> reply(@PathVariable Long commentId,
                                    @RequestBody ReplyDto dto,
                                    HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return Result.success(petCommentService.replyComment(commentId, userId, dto));
    }

    @ApiOperation("点赞评论")
    @PostMapping("/comments/{commentId}/like")
    public Result<String> like(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petCommentService.likeComment(commentId, userId);
        return Result.success("点赞成功");
    }

    @ApiOperation("取消点赞")
    @DeleteMapping("/comments/{commentId}/like")
    public Result<String> unlike(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petCommentService.unlikeComment(commentId, userId);
        return Result.success("已取消点赞");
    }

    @ApiOperation("心碎（踩）")
    @PostMapping("/comments/{commentId}/dislike")
    public Result<String> dislike(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petCommentService.dislikeComment(commentId, userId);
        return Result.success("💔");
    }

    @ApiOperation("取消心碎")
    @DeleteMapping("/comments/{commentId}/dislike")
    public Result<String> undislike(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petCommentService.undislikeComment(commentId, userId);
        return Result.success("已取消心碎");
    }

    @ApiOperation("隐藏评论（送养人）")
    @PutMapping("/comments/{commentId}/hide")
    public Result<String> hide(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petCommentService.hideComment(commentId, userId);
        return Result.success("已删除");
    }

    @ApiOperation("删除自己的评论")
    @DeleteMapping("/comments/{commentId}")
    public Result<String> delete(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        petCommentService.deleteComment(commentId, userId);
        return Result.success("已删除");
    }
}
