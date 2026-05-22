package com.pet.module.pet.mapper;

import com.pet.module.pet.model.entity.PetComment;
import com.pet.module.pet.model.entity.PetCommentLike;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PetCommentMapper {

    int insert(PetComment comment);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    PetComment selectById(Long id);

    /** 根评论列表 */
    List<PetComment> selectRootByPetId(@Param("petId") Long petId,
                                        @Param("sort") String sort,
                                        @Param("statusLimit") Integer statusLimit);

    /** 子回复列表 */
    List<PetComment> selectReplies(@Param("petId") Long petId, @Param("parentId") Long parentId);

    int countByPetId(Long petId);

    // ===== 点赞 =====
    int insertLike(PetCommentLike like);

    int updateLikeCount(@Param("commentId") Long commentId);

    int deleteLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    PetCommentLike selectLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    int countLikeByCommentId(Long commentId);

    // ===== 心碎 =====
    int insertDislike(PetCommentLike like);

    int updateDislikeCount(@Param("commentId") Long commentId);

    int deleteDislike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    PetCommentLike selectDislike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    int deleteById(Long id);

    int deleteByParentId(Long parentId);
}
