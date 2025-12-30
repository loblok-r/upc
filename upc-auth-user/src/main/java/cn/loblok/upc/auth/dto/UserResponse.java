package cn.loblok.upc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserResponse {
    @Schema(description = "用户ID")
private String id;

}