package cn.loblok.upc.auth.dto;

import cn.loblok.upc.auth.constraints.CheckVerificationCodeGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


/**
 * 登录请求参数
 */
@Data
public class LoginRequestDTO {

    /**
     * 邮箱
     */

    @NotBlank(message = "邮箱不可以为空")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式错误")
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不可以为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{6,16}$",message = "请输入6-16位密码")
    private String password;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不可以为空",groups = {CheckVerificationCodeGroup.class})
    @Pattern(regexp = "^\\d{6}$",message = "请输入6位验证码",groups = {CheckVerificationCodeGroup.class})
    private String code;
}