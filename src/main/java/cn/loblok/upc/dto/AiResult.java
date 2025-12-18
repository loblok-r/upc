package cn.loblok.upc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiResult {
    private String content;
    private String imageUrl; //展示内容
    private String cosPath; //cos path
}