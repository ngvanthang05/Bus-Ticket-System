package com.xekhach.tripservice.exception;

/**
 * Ném ra khi gọi Feign tới Route/Vehicle Service thất bại
 * (service down, timeout, route/vehicle không tồn tại, v.v.)
 */
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}