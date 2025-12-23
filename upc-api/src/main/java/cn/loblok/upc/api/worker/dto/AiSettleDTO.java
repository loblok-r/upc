package cn.loblok.upc.api.worker.dto;

import cn.loblok.upc.common.enums.AppMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiSettleDTO {

    private Long userId;

    private Integer cost;

    private AppMode mode;

    private String sessionId;

    private String prompt;

    private String refImage;

    private String content;

    private String cosPath;

}