package com.pet.module.pet.service;

import com.pet.module.pet.model.dto.CommentDto;
import com.pet.module.pet.model.dto.ReplyDto;
import com.pet.module.pet.model.vo.CommentVo;

import java.util.List;
import java.util.Map;

public interface PetCommentService {

    CommentVo addComment(Long petId, Long userId, CommentDto dto);

    CommentVo replyComment(Long commentId, Long userId, ReplyDto dto);

    Map<String, Object> getComments(Long petId, Long userId, String sort, int page, int size);

    void likeComment(Long commentId, Long userId);

    void unlikeComment(Long commentId, Long userId);

    void dislikeComment(Long commentId, Long userId);

    void undislikeComment(Long commentId, Long userId);

    void hideComment(Long commentId, Long userId);

    void deleteComment(Long commentId, Long userId);
}
