package cn.loblok.upc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应体
 */

@Data
public class AuthResponseDTO {

    /**
     * 登录成功返回的用户id
     */
    @Schema(description = "用户id")
    private Long userId;


    /**
     * 登录成功返回的token
     */
    @Schema(description = "token")
    private String token;


    /**
     * 登录成功返回的token过期时间
     */
    @Schema(description = "token过期时间")
    private long expiresIn;



}