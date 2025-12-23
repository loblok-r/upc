package cn.loblok.upc.common.enums;

import lombok.Getter;

/**
 * @author loblok
 * @date 2021/12/23
 */

@Getter
public enum PostsTab {
    RECOMMEND("recommend"),
    LATEST("latest"),
    FOLLOW("follow"),
    MINE("mine"),
    SOMEONTE("someone");

    private final String description;

    PostsTab(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
