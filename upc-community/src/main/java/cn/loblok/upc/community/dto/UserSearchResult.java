package cn.loblok.upc.community.dto;

import lombok.Data;

@Data
public class UserSearchResult {
    private Long id;
    private String username;
    private String avatarUrl;
    private Integer followers;
    private Integer isFollowed;
}