package cn.loblok.upc.enums;

import lombok.Getter;

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