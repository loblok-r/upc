package cn.loblok.upc.modules.mall.entity;

import cn.loblok.upc.common.enums.BizType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
/**
 * 优惠券发放上下文
 */
@Data
@Builder
public class IssueContext {
    private BizType bizType;      // 必填
    private String bizId;        // 必填，幂等键
    private Map<String, Object> extInfo; // 可选
}