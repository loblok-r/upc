package cn.loblok.upc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CouponResponse {

    @Schema(description = "优惠券ID")
    private String id; // 优惠券ID
    @Schema(description = "优惠券状态")
    private  String status ; // 优惠券状态 1:未使用 2:已使用 3:已过期
    @Schema(description = "优惠券类型")
    private String  type; // 优惠券类型
    @Schema(description = "优惠券名称")
    private  String name; // 优惠券名称
    @Schema(description = "优惠券金额")
    private LocalDate expiryDate; // 过期时间
    @Schema(description = "优惠金额")
    private String discount ; // 优惠金额

}