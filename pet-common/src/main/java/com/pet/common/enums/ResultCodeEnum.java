package com.pet.common.enums;

import com.pet.common.constant.ApiConstant;

/**
 * 统一返回码枚举
 */
public enum ResultCodeEnum {

    // 成功
    SUCCESS(ApiConstant.SUCCESS, "操作成功"),

    // 400 系列 — 请求/参数错误
    BAD_REQUEST(ApiConstant.BAD_REQUEST, "请求参数错误"),
    PARAM_MISSING(4001, "缺少必要参数"),
    PARAM_INVALID(4002, "参数格式错误"),
    USERNAME_EXISTS(4003, "用户名已存在"),
    PASSWORD_INCORRECT(4004, "密码错误"),
    OLD_PASSWORD_ERROR(4005, "原密码错误"),
    PHONE_EXISTS(4006, "手机号已被绑定"),
    FILE_EMPTY(4007, "上传文件为空"),
    FILE_TYPE_ERROR(4008, "文件类型不支持"),
    FILE_SIZE_EXCEED(4009, "文件大小超出限制"),
    PET_IMAGE_MIN(4010, "宠物图片至少上传3张"),
    EXAM_NOT_PASS(4020, "领养考试未通过"),

    // 401 系列 — 认证
    TOKEN_INVALID(4011, "Token无效"),
    TOKEN_EXPIRED(4012, "Token已过期"),

    // 403 系列 — 权限
    USER_DISABLED(4031, "账号已被禁用"),
    ROLE_REQUIRED(4032, "角色权限不足"),

    // 404 系列 — 资源不存在
    USER_NOT_FOUND(4041, "用户不存在"),
    PET_NOT_FOUND(4042, "宠物信息不存在"),
    CATEGORY_NOT_FOUND(4043, "分类不存在"),
    ORDER_NOT_FOUND(4044, "订单不存在"),
    PRODUCT_NOT_FOUND(4045, "商品不存在"),
    FEEDBACK_NOT_FOUND(4046, "反馈不存在"),
    NOTICE_NOT_FOUND(4047, "公告不存在"),
    QUESTION_NOT_FOUND(4048, "试题不存在"),
    APPLICATION_NOT_FOUND(4049, "领养申请不存在"),

    // 500 系列 — 服务端
    FILE_UPLOAD_ERROR(5001, "文件上传失败"),
    AI_SERVICE_ERROR(5002, "AI服务调用失败"),
    DB_ERROR(5003, "数据库操作失败"),
    UNKNOWN_ERROR(5999, "未知错误");

    private final int code;
    private final String message;

    ResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}