package cn.loblok.upc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MallOrderStatus {


    //待发放
    PENDING_DELIVERY(1,"PENDING_DELIVERY"),

    //待收货
    PENDING_EXTERNAL(2,"PENDING_EXTERNAL"),
    //待发货
    AWAITING_SHIPMENT(3,"AWAITING_SHIPMENT"),

    FAIL(4,"FAIL"),

    COMPLETED(5,"COMPLETED"),;


    @EnumValue
    private final int code;

    private final String status;

    MallOrderStatus(int code, String status) {
        this.code = code;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public boolean toContrains(MallOrderStatus targetStatus) {
        switch (this) {
            case PENDING_DELIVERY:
                return targetStatus == COMPLETED || targetStatus == FAIL || targetStatus == PENDING_EXTERNAL || targetStatus == AWAITING_SHIPMENT;
            case COMPLETED:
                return false;
            case PENDING_EXTERNAL:
                return targetStatus == COMPLETED || targetStatus == FAIL;
            case AWAITING_SHIPMENT:
                return targetStatus == PENDING_EXTERNAL || targetStatus == FAIL;
            case FAIL:
                return false;
            default:
                return false;
        }
    }

}