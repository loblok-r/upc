package cn.loblok.upc.enums;

import lombok.Getter;

@Getter
public enum PayMethodEnum {
    ALI_PAY("alipay"),
    WECHAT_PAY("wechat");

    private final String code;

    PayMethodEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}