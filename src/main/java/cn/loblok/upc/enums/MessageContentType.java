package cn.loblok.upc.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MessageContentType {

    TEXT("text"),
    IMAGE("image"),
    AUDIO("audio"),
    VIDEO("video"),
    FILE("file"),
    UNKNOWN("unknown");

    @EnumValue
    private final String value;

    MessageContentType(String value) {
        this.value = value;
    }
}
