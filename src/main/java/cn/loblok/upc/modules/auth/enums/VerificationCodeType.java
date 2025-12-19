package cn.loblok.upc.modules.auth.enums;

import lombok.Getter;

/**
 * 验证码类型枚举
 */
@Getter
public enum VerificationCodeType{

    register("register"),
    login("login"),
    forget("forgot");

    private VerificationCodeType(String type){
        this.type = type;
    }

    private String type;

}