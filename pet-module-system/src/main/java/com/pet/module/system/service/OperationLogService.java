package com.pet.module.system.service;

import com.pet.module.system.model.entity.SysOperationLog;

import java.util.List;

public interface OperationLogService {

    void addLog(Long userId, String username, String module, String action, String ip);

    List<SysOperationLog> getLogList(String module, int page, int size);
}