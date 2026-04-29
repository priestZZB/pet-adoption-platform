package com.pet.common.exception;

import com.pet.common.enums.ResultCodeEnum;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCodeEnum resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCodeEnum resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() { return code; }
}