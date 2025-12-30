package cn.loblok.upc.trade.dto.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户付款表单数据
 */
@Data
public class UserPayFormData {
    @Schema(description = "用户名")
    private String firstName;
    @Schema(description = "用户名")
    private String lastName;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "备注")
    private String notes;
    @Schema(description = "支付方式")
    private String paymentMethod;
}