package cn.loblok.upc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdateProfileRequestDTO {

    @Schema(description = "邮件")
    private String email;
    
    @Schema(description = "头像")
    private String avatar; // "avatar": "https://example.com/avatar.jpg"

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;
}