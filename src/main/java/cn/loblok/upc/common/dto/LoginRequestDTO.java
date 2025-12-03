package cn.loblok.upc.common.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String username;
    private String password;
}