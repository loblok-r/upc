package cn.loblok.upc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ValidationResult {
    @Schema(description = "验证结果")
    private boolean valid;
    @Schema(description = "验证消息")
    private String message;
    @Schema(description = "验证数据")
    private Object data; // 可选，用于携带额外信息

    // 构造器
    public ValidationResult(boolean valid) {
        this.valid = valid;
    }

    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public ValidationResult(boolean valid, String message, Object data) {
        this.valid = valid;
        this.message = message;
        this.data = data;
    }

    // 静态工厂方法
    public static ValidationResult success() {
        return new ValidationResult(true);
    }

    public static ValidationResult success(String message) {
        return new ValidationResult(true, message);
    }

    public static ValidationResult success(Object data) {
        ValidationResult result = new ValidationResult(true);
        result.setData(data);
        return result;
    }

    public static ValidationResult error(String message) {
        return new ValidationResult(false, message);
    }

    public static ValidationResult error(String message, Object data) {
        return new ValidationResult(false, message, data);
    }

    // 便捷方法
    public boolean isValid() {
        return valid;
    }

    public boolean isError() {
        return !valid;
    }
}