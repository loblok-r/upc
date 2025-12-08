package cn.loblok.upc.dto;

import cn.loblok.upc.constraints.CheckVerificationCodeGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不可以为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{3,20}$",message = "用户名格式错误")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不可以为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$",message = "密码格式错误")
    private String password;


    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不可以为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$",message = "密码格式错误")
    private String confirmPassword;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不可以为空")
    @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$",message = "请输入正确的邮箱")
    private String email;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不可以为空",groups = {CheckVerificationCodeGroup.class})
    @Pattern(regexp = "^\\d{6}$",message = "请输入6位验证码",groups = {CheckVerificationCodeGroup.class})
    private String code;

}