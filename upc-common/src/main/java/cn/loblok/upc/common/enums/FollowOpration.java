package cn.loblok.upc.common.enums;

import lombok.Getter;

@Getter
public enum FollowOpration {
    FOLLOW(1),
    UNFOLLOW(-1);

    private final int value;

    FollowOpration(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
