package cn.loblok.upc.common.exception;

/**
 * 日限额超出异常
 */
public class DailyLimitExceededException extends BusinessException {
    
    public DailyLimitExceededException(String message) {
        super(message);
    }
    
    public DailyLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}