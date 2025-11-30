package cn.loblok.upc.dto;

import lombok.Data;

@Data
public class CheckinResponseDTO {
    private Long checkinRecordId;
    private Boolean success;
    private String message;
}