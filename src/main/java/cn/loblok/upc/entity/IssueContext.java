package cn.loblok.upc.entity;

import cn.loblok.upc.enums.BizType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class IssueContext {
    private BizType bizType;      // 必填
    private String bizId;        // 必填，幂等键
    private Map<String, Object> extInfo; // 可选
}