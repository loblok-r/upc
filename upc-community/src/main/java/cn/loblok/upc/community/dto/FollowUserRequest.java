package cn.loblok.upc.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * 关注用户
 */
@Data
public class FollowUserRequest {
    @Schema(description = "用户ID")
    private Long userId;
    // getters and setters
}