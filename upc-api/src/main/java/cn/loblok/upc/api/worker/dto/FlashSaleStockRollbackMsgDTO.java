package cn.loblok.upc.api.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlashSaleStockRollbackMsgDTO implements Serializable {
    private String flashSaleId;
    private Long userId;
    private String reason;
}