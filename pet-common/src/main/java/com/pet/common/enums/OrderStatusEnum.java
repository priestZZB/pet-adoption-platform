package com.pet.common.enums;

/**
 * 订单状态枚举
 */
public enum OrderStatusEnum {

    PENDING_PAY("PENDING_PAY", "待支付"),
    PAID("PAID", "已支付"),
    SHIPPED("SHIPPED", "已发货"),
    RECEIVED("RECEIVED", "已收货"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    OrderStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static OrderStatusEnum getByCode(String code) {
        for (OrderStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}