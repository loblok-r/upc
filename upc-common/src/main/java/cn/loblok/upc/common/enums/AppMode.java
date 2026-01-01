package cn.loblok.upc.common.enums;

import lombok.Getter;

@Getter
public enum AppMode {
    TEXT_CHAT("text_chat"),
    AI_DRAWING("ai_drawing"),
    SMART_PRESENTATION("smart_presentation"),
    PODCAST("podcast")
    ;
    private final String mode;

    AppMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public static AppMode fromMode(String mode) {
        for (AppMode value : values()) {
            if (value.mode.equals(mode)) {
                return value;
            }
        }
        return null;
    }

}
