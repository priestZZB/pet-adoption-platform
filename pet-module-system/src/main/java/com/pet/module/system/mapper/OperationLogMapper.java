package com.pet.module.system.mapper;

import com.pet.module.system.model.entity.SysOperationLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OperationLogMapper {

    int insert(SysOperationLog log);

    List<SysOperationLog> selectPage(@Param("module") String module);
}