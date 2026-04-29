package com.pet.common.exception;

import com.pet.common.enums.ResultCodeEnum;

/**
 * 未授权异常（401/403）
 */
public class UnauthorizedException extends RuntimeException {

    private final int code;

    public UnauthorizedException(ResultCodeEnum resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public UnauthorizedException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() { return code; }
}