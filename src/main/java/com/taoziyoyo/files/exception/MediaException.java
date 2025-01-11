package com.taoziyoyo.files.exception;

import lombok.Getter;

@Getter
public class MediaException extends RuntimeException {

    private final int code;

    public MediaException(String message) {
        super(message);
        this.code = 500;
    }

    public MediaException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public MediaException(int code, String message) {
        super(message);
        this.code = code;
    }

    public MediaException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public static MediaException notFound(String message) {
        return new MediaException(404, message);
    }

    public static MediaException badRequest(String message) {
        return new MediaException(400, message);
    }

    public static MediaException unauthorized(String message) {
        return new MediaException(401, message);
    }

    public static MediaException forbidden(String message) {
        return new MediaException(403, message);
    }
}