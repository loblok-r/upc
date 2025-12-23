package cn.loblok.upc.api.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// cn.loblok.upc.api.worker.chickin.EmailMsgDTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailMsgDTO implements Serializable {
    private String email;
    private String code;
    private String type; // login, register, forgot
}