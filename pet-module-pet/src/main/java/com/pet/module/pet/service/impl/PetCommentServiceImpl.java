package com.pet.module.pet.service.impl;

import com.github.pagehelper.PageHelper;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.event.NotificationEvent;
import com.pet.common.exception.BusinessException;
import com.pet.module.pet.mapper.PetCommentMapper;
import com.pet.module.pet.mapper.PetInfoMapper;
import com.pet.module.pet.model.dto.CommentDto;
import com.pet.module.pet.model.dto.ReplyDto;
import com.pet.module.pet.model.entity.PetComment;
import com.pet.module.pet.model.entity.PetCommentLike;
import com.pet.module.pet.model.entity.PetInfo;
import com.pet.module.pet.model.vo.CommentVo;
import com.pet.module.pet.service.PetCommentService;
import com.pet.module.system.mapper.UserMapper;
import com.pet.module.system.model.entity.SysUser;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PetCommentServiceImpl implements PetCommentService {

    private final PetCommentMapper commentMapper;

    private final PetInfoMapper petInfoMapper;

    private final UserMapper userMapper;

    private final ApplicationEventPublisher eventPublisher;

    public PetCommentServiceImpl(
            PetCommentMapper commentMapper,
            PetInfoMapper petInfoMapper,
            UserMapper userMapper,
            ApplicationEventPublisher eventPublisher) {
        this.commentMapper = commentMapper;
        this.petInfoMapper = petInfoMapper;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
    }


    @Override
    @Transactional
    public CommentVo addComment(Long petId, Long userId, CommentDto dto) {
        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null) throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND, "宠物不存在");

        PetComment comment = new PetComment();
        comment.setPetId(petId);
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            comment.setImages(String.join(",", dto.getImages()));
        }
        commentMapper.insert(comment);

        // 通知送养人：有人评论了你的宠物
        if (!pet.getUserId().equals(userId)) {
            SysUser commenter = userMapper.selectById(userId);
            String name = commenter != null ? (commenter.getNickname() != null ? commenter.getNickname() : commenter.getUsername()) : "用户";
            String petName = pet.getName() != null ? pet.getName() : "宠物";
            eventPublisher.publishEvent(new NotificationEvent(
                    pet.getUserId(), "PET_COMMENT",
                    "宠物收到新评论",
                    name + "评论了你发布的" + petName,
                    petId));
        }

        return toVo(comment, userId);
    }

    @Override
    @Transactional
    public CommentVo replyComment(Long commentId, Long userId, ReplyDto dto) {
        PetComment parent = commentMapper.selectById(commentId);
        if (parent == null) throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "评论不存在");

        if (dto.getReplyTo() == null) throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "请指定回复目标");

        PetComment reply = new PetComment();
        reply.setPetId(parent.getPetId());
        reply.setUserId(userId);
        reply.setParentId(commentId);
        reply.setReplyTo(dto.getReplyTo());
        reply.setContent(dto.getContent());
        commentMapper.insert(reply);

        // 通知被回复的人
        SysUser replier = userMapper.selectById(userId);
        String replierName = replier != null ? (replier.getNickname() != null ? replier.getNickname() : replier.getUsername()) : "用户";
        String replySnippet = dto.getContent() != null && dto.getContent().length() > 20 ? dto.getContent().substring(0, 20) + "…" : dto.getContent();

        // 通知父评论的作者（如果不等于回复人自己）
        if (!parent.getUserId().equals(userId)) {
            eventPublisher.publishEvent(new NotificationEvent(
                    parent.getUserId(), "PET_COMMENT_REPLY",
                    "回复了你的评论",
                    replierName + "回复了你的评论：“" + replySnippet + "”",
                    parent.getPetId()));
        }

        // 如果 replyTo 和父评论作者不是同一个人，额外通知 replyTo 的人
        if (dto.getReplyTo() != null && !dto.getReplyTo().equals(parent.getUserId()) && !dto.getReplyTo().equals(userId)) {
            eventPublisher.publishEvent(new NotificationEvent(
                    dto.getReplyTo(), "PET_COMMENT_REPLY",
                    "有人回复了你",
                    replierName + "回复了你：“" + replySnippet + "”",
                    parent.getPetId()));
        }

        return toVo(reply, userId);
    }

    @Override
    public Map<String, Object> getComments(Long petId, Long userId, String sort, int page, int size) {
        PageHelper.startPage(page, size);
        List<PetComment> roots = commentMapper.selectRootByPetId(petId, sort, null);

        long total = commentMapper.countByPetId(petId);

        List<CommentVo> list = roots.stream().map(c -> {
            CommentVo vo = toVo(c, userId);
            List<PetComment> replies = commentMapper.selectReplies(petId, c.getId());
            vo.setReplies(replies.stream().map(r -> toVo(r, userId)).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        PetComment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "评论不存在");
        PetCommentLike existing = commentMapper.selectLike(commentId, userId);
        if (existing != null) throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "已点赞");

        PetCommentLike like = new PetCommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        commentMapper.insertLike(like);
        commentMapper.updateLikeCount(commentId);

        // 通知评论作者有人点赞
        if (!comment.getUserId().equals(userId)) {
            SysUser liker = userMapper.selectById(userId);
            String name = liker != null ? (liker.getNickname() != null ? liker.getNickname() : liker.getUsername()) : "用户";
            eventPublisher.publishEvent(new NotificationEvent(
                    comment.getUserId(), "PET_COMMENT_LIKE",
                    "评论收到点赞",
                    name + "赞了你的评论",
                    comment.getPetId()));
        }
    }

    @Override
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        PetCommentLike existing = commentMapper.selectLike(commentId, userId);
        if (existing == null) throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "未点赞");
        commentMapper.deleteLike(commentId, userId);
        commentMapper.updateLikeCount(commentId);
    }

    @Override
    @Transactional
    public void dislikeComment(Long commentId, Long userId) {
        PetComment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "评论不存在");
        PetCommentLike existing = commentMapper.selectDislike(commentId, userId);
        if (existing != null) throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "已心碎");
        PetCommentLike dislike = new PetCommentLike();
        dislike.setCommentId(commentId);
        dislike.setUserId(userId);
        commentMapper.insertDislike(dislike);
        commentMapper.updateDislikeCount(commentId);

        // 通知评论作者被踩
        if (!comment.getUserId().equals(userId)) {
            SysUser disliker = userMapper.selectById(userId);
            String name = disliker != null ? (disliker.getNickname() != null ? disliker.getNickname() : disliker.getUsername()) : "用户";
            eventPublisher.publishEvent(new NotificationEvent(
                    comment.getUserId(), "PET_COMMENT_DISLIKE",
                    "评论收到心碎",
                    name + "给你的评论点了个心碎💔",
                    comment.getPetId()));
        }
    }

    @Override
    @Transactional
    public void undislikeComment(Long commentId, Long userId) {
        PetCommentLike existing = commentMapper.selectDislike(commentId, userId);
        if (existing == null) throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "未心碎");
        commentMapper.deleteDislike(commentId, userId);
        commentMapper.updateDislikeCount(commentId);
    }

    @Override
    @Transactional
    public void hideComment(Long commentId, Long userId) {
        PetComment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "评论不存在");

        PetInfo pet = petInfoMapper.selectById(comment.getPetId());
        if (pet == null) throw new BusinessException(ResultCodeEnum.PET_NOT_FOUND, "宠物不存在");
        // 送养人或管理员可删除
        if (!pet.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.ROLE_REQUIRED, "只有送养人可以删除评论");
        }
        // 硬删除：评论及相关点赞/心碎由数据库 CASCADE 自动清理
        commentMapper.deleteById(commentId);
        // 同时删除该评论下的所有回复
        commentMapper.deleteByParentId(commentId);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        PetComment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "评论不存在");
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.ROLE_REQUIRED, "只能删除自己的评论");
        }
        commentMapper.updateStatus(commentId, 0);
    }

    private CommentVo toVo(PetComment comment, Long currentUserId) {
        CommentVo vo = new CommentVo();
        BeanUtils.copyProperties(comment, vo);

        if (comment.getImages() != null && !comment.getImages().isEmpty()) {
            vo.setImages(Arrays.asList(comment.getImages().split(",")));
        }

        SysUser user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setAvatar(user.getAvatar());
        }

        if (comment.getReplyTo() != null) {
            SysUser replyUser = userMapper.selectById(comment.getReplyTo());
            if (replyUser != null) {
                vo.setReplyToName(replyUser.getNickname() != null ? replyUser.getNickname() : replyUser.getUsername());
            }
        }

        if (currentUserId != null) {
            vo.setLiked(commentMapper.selectLike(comment.getId(), currentUserId) != null);
            vo.setDisliked(commentMapper.selectDislike(comment.getId(), currentUserId) != null);
        }
        vo.setDislikeCount(comment.getDislikeCount());

        return vo;
    }
}
