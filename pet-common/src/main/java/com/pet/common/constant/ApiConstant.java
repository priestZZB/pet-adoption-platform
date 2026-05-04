package com.pet.common.constant;

/**
 * API 状态码常量
 */
public interface ApiConstant {

    int SUCCESS = 200;

    // ========== 客户端错误 ==========
    int BAD_REQUEST = 400;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int METHOD_NOT_ALLOWED = 405;
    int CONFLICT = 409;

    // ========== 服务端错误 ==========
    int INTERNAL_SERVER_ERROR = 500;
    int BAD_GATEWAY = 502;
    int SERVICE_UNAVAILABLE = 503;
}