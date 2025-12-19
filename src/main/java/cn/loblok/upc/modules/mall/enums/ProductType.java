package cn.loblok.upc.modules.mall.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 产品类型枚举
 */
public enum ProductType {


    PHYSICAL("physical"),
    VIRTUAL("virtual"),
    VOUCHER("voucher");


    @EnumValue
    private final String value;


    ProductType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}