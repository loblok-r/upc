package cn.loblok.upc.exception;

/**
 * 算力不足异常
 */
public class InsufficientComputingPowerException extends BusinessException {
    
    public InsufficientComputingPowerException(String message) {
        super(message);
    }
    
    public InsufficientComputingPowerException(String message, Throwable cause) {
        super(message, cause);
    }
}