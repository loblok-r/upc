package cn.loblok.upc.dto;

import lombok.Data;


/**
 * 关注用户
 */
@Data
public class FollowUserRequest {
    private Long targetUserId;
    // getters and setters
}