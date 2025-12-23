package cn.loblok.upc.trade.dto.pay;

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