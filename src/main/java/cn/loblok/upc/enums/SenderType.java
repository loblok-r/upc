package cn.loblok.upc.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum SenderType {

    USER("User"),
    AI("AI");

    @EnumValue
    private final String value;

    SenderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}