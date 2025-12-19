package cn.loblok.upc.modules.payment.dto;

import lombok.Data;

/**
 * 用户付款表单数据
 */
@Data
public class UserPayFormData {
    private String firstName;
    private String lastName;
    private String email;
    private String notes;
    private String paymentMethod;
}