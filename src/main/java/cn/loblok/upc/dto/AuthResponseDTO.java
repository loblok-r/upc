package cn.loblok.upc.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {

    private String token;
    private String username;
    private Long userId;

}