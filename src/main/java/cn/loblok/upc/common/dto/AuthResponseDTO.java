package cn.loblok.upc.common.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private Long userId;
    private String username;
}