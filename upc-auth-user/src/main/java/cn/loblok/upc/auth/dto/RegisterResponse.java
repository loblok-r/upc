package cn.loblok.upc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RegisterResponse {
    @Schema(description = "注册结果")
    private String msg;
}