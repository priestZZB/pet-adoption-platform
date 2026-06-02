package com.pet.module.adopt.mapper;

import com.pet.module.adopt.model.entity.AdoptQuestion;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AdoptQuestionMapper {

    int insert(AdoptQuestion question);

    int updateById(AdoptQuestion question);

    int deleteById(Long id);

    AdoptQuestion selectById(Long id);

    List<AdoptQuestion> selectAll();

    List<AdoptQuestion> selectRandom();

    /**
     * 批量删除试题
     */
    int batchDelete(@Param("ids") List<Long> ids);

    /**
     * 批量插入试题（Excel/CSV 导入）
     */
    int batchInsert(@Param("list") List<AdoptQuestion> list);
}