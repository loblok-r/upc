package cn.loblok.upc.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CouponResponse {

    private String id; // 优惠券ID
    private  String status ; // 优惠券状态 1:未使用 2:已使用 3:已过期
    private String  type; // 优惠券类型
    private  String name; // 优惠券名称
    private LocalDate expiryDate; // 过期时间
    private String discount ; // 优惠金额

}