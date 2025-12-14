package cn.loblok.upc.enums;

import lombok.Getter;

@Getter
public enum TOrderStatus {

    PENDING("PENDING", "待支付"),
    SUCCESS("SUCCESS", "支付成功"),
    FAILED("FAILED", "支付失败"),
    EXPIRED("EXPIRED", "支付过期");

    private final String code;
    private final String description;

    TOrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TOrderStatus fromCode(String code) {
        for (TOrderStatus status : TOrderStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + code + "]");
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isExpired() {
        return this == EXPIRED;
    }


    public boolean canTransitionTo(TOrderStatus nextStatus) {
        switch (this) {
            case PENDING:
                return nextStatus == SUCCESS || nextStatus == FAILED || nextStatus == EXPIRED;
            case SUCCESS:
            case FAILED:
            case EXPIRED:
                return false;
            default:
                return false;
        }
    }
}