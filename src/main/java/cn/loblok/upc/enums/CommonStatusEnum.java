package cn.loblok.upc.enums;

import lombok.Getter;

/**
 * 通用状态枚举
 *
 * @author loblok
 * @date 2025/12/01
 */
public enum CommonStatusEnum {

    CALL_USER_ADD_ERROR(1000,"新增用户错误"),

    CHECK_CODE_ERROR(1001,"检查验证码错误"),
    /**
     * 全局校验错误
     */
    VALIDATION_ERROR(1700,"全局校验出现错误"),
    /**
     * TOKEN错误状态
     */
    TOKEN_ERROR(1199,"TOKEN错误"),
    /**
     * 验证码不正确
     */

    VERIFICATION_CODE_ERROR(1099,"验证码不正确"),


    USER_EMPTY_ERROR(1299,"用户为空"),

    USER_EXIST(1300,"用户已存在"),

    /**
     * 成功状态
     */
    SUCCESS(200,"success"),


    /**
     * 失败状态
     */

    ERROR(0,"error"),


    PASSWORD_NOT_MATCH(1002,"密码不匹配"),


    USER_PASSWORD_ERROR(1003,"用户密码错误"),

    HasCheckedIn(7001,"已经签到过了"),
    
    USER_NOT_FOUND(1004, "用户不存在"),
    
    CODE_ERROR(1005, "验证码错误"),
    
    PARAM_ERROR(1006, "参数错误"),

    USER_CANNOT_EMPTY(1007, "用户ID不能为空"),

    INSUFFICIENT_COMPUTING_POWER(1008, "INSUFFICIENT_COMPUTING_POWER"),
    
    DAILY_LIMIT_EXCEEDED(1009, "DAILY_LIMIT_EXCEEDED"),
    INVALID_ARGUMENT(1010, "无效的参数"),
    INTERNAL_SERVER_ERROR(1011, "服务器内部错误"),
    UNAUTHORIZED(1012, "未授权");

    @Getter
    private int code;
    @Getter
    private String message;

    CommonStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}