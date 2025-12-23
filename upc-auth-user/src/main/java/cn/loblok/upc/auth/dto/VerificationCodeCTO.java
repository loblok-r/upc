package cn.loblok.upc.auth.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 验证码请求参数
 */
@Data
public class VerificationCodeCTO {

    @NotBlank(message = "邮箱不可以为空")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式错误")
    private String email;


    @NotBlank(message = "验证码类型不可以为空")
    private String type;

}