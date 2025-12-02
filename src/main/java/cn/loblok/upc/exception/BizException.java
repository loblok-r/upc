package cn.loblok.upc.exception;

import lombok.Data;

/**
 * 业务异常类
 */

@Data
public class BizException extends RuntimeException {
    
    private int code = 500;
    
    public BizException(String message) {
        super(message);
    }
    
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BizException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}