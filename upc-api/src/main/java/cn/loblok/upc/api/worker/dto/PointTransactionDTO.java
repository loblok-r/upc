package cn.loblok.upc.api.worker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointTransactionDTO {
    private static final long serialVersionUID = 1L;

    private String tenantId;
    private Long userId;
    private String bizType;      // 或者用 enum，但需确保 MQ 能序列化
    private Long bizId;          // 注意：这里保留为 Long，消费者转 String
    private Integer deltaPoints;
    private Integer totalPoints;

    private LocalDateTime occurredAt; // 可选：记录事件发生时间（非消费时间）

}