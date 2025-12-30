package cn.loblok.upc.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 关注用户
 */
@Data
public class FollowUserResponse {

    @Schema(description = "被关注者的粉丝数")
    private Integer followerCount; // 被关注者的粉丝数
    // getters and setters
}