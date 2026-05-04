package com.pet.module.system.service.impl;

import com.github.pagehelper.PageHelper;
import com.pet.module.system.mapper.OperationLogMapper;
import com.pet.module.system.model.entity.SysOperationLog;
import com.pet.module.system.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public void addLog(Long userId, String username, String module, String action, String ip) {
        SysOperationLog log = new SysOperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setModule(module);
        log.setAction(action);
        log.setIp(ip);
        operationLogMapper.insert(log);
    }

    @Override
    public List<SysOperationLog> getLogList(String module, int page, int size) {
        PageHelper.startPage(page, size);
        return operationLogMapper.selectPage(module);
    }
}