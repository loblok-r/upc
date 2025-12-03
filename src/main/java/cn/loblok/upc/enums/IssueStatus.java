package cn.loblok.upc.enums;

public enum IssueStatus {
    SUCCESS((byte) 0, "发放成功"),
    FAILED((byte) 1, "发放失败");

    private final byte code;
    private final String desc;

    IssueStatus(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    // 可选：提供根据 code 反查枚举的方法（用于 DB 查询结果映射）
    public static IssueStatus fromCode(Byte code) {
        if (code == null) return null;
        for (IssueStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown issue status code: " + code);
    }
}
