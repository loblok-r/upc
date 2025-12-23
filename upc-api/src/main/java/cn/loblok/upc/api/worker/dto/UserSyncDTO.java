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
public class UserSyncDTO implements Serializable {
    private Long id;
    private String username;
    private String avatarUrl;
    private Integer followers;
    private Integer status;   // 0-正常 1-禁用
    private String operation; // "UPSERT" (新增或更新) 或 "DELETE" (删除)
}