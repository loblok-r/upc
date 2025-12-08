package cn.loblok.upc.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {

    /**
     * 登录成功返回的token
     */

    private String token;

    /**
     * 登录成功返回的用户名
     */
    private String username;

    /**
     * 登录成功返回的用户id
     */
    private Long userId;


    /**
     * 登录成功返回的过期时间
     */
    private Long expiresIn;

    /**
     * 登录成功返回的头像
     */
    private  String avatar; //"avatar": "https://example.com/avatar.jpg"

}