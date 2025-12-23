package cn.loblok.upc.common.enums;

import lombok.Getter;

@Getter
public enum AppMode {
    TEXT_CHAT("TEXT_CHAT"),
    AI_DRAWING("AI_DRAWING"),
    SMART_PRESENTATION("SMART_PRESENTATION"),
    PODCAST("PODCAST")
    ;
    private final String mode;

    AppMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

}
