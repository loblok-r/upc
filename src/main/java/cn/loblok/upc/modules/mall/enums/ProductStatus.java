package cn.loblok.upc.modules.mall.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ProductStatus {


    ACTIVE("active"),
    SOLD_OUT("sold_out"),
    INACTIVE("inactive");

    @EnumValue
    private final String value;

    ProductStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
