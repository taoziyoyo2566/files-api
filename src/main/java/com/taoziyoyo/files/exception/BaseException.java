package com.taoziyoyo.files.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    private final int code;
    private final String errorCode;

    protected BaseException(int code, String message) {
        super(message);
        this.code = code;
        this.errorCode = null;
    }

    protected BaseException(int code, String errorCode, String message) {
        super(message);
        this.code = code;
        this.errorCode = errorCode;
    }

    protected BaseException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.errorCode = null;
    }

    protected BaseException(int code, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.errorCode = errorCode;
    }
}