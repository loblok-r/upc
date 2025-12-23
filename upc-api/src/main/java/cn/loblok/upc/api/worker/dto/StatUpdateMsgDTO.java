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
public class StatUpdateMsgDTO implements Serializable {
    private Long userId;
    private Long targetUserId;
    private String type;   // LIKE, POST, FOLLOW
    private Integer delta; // 1 (增加), -1 (减少)
}