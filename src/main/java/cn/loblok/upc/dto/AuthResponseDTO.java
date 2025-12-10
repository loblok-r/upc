package cn.loblok.upc.dto;

import lombok.Data;

/**
 * 登录响应体
 */

@Data
public class AuthResponseDTO {

    /**
     * 登录成功返回的用户id
     */
    private Long userId;


    /**
     * 登录成功返回的token
     */

    private String token;


    /**
     * 登录成功返回的token过期时间
     */
    private long expiresIn;



}