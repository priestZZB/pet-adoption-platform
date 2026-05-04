package com.pet.common.enums;

/**
 * 宠物状态枚举
 */
public enum PetStatusEnum {

    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "审核通过"),
    REJECTED("REJECTED", "已打回"),
    ADOPTED("ADOPTED", "已领养"),
    OFFLINE("OFFLINE", "已下架");

    private final String code;
    private final String desc;

    PetStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static PetStatusEnum getByCode(String code) {
        for (PetStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}