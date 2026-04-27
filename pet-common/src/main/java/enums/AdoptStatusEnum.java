package enums;

/**
 * 领养申请状态枚举
 */
public enum AdoptStatusEnum {

    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String desc;

    AdoptStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static AdoptStatusEnum getByCode(String code) {
        for (AdoptStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}