package cn.loblok.upc.dto;

import lombok.Data;

/**
 * 关注用户
 */
@Data
public class FollowUserResponse {

    private Integer followerCount; // 被关注者的粉丝数
    // getters and setters
}