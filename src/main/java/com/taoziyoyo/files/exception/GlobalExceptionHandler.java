package com.taoziyoyo.files.exception;

import com.taoziyoyo.files.model.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理业务异常
    @ExceptionHandler(BaseException.class)
    public ApiResponse<Void> handleBaseException(BaseException e) {
        log.error("Business error: {}", e.getMessage(), e);
        return ApiResponse.error(e.getCode(), e.getErrorCode(), e.getMessage());
    }

    // 处理参数验证失败异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("Validation failed: {}", message);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", message);
    }

    // 处理约束违反异常
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        log.error("Constraint violation: {}", message);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "CONSTRAINT_VIOLATION", message);
    }

    // 处理请求体解析失败异常
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("Message not readable", e);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "INVALID_REQUEST", "Invalid request body format");
    }

    // 处理媒体类型不支持异常
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ApiResponse<Void> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.error("Media type not supported", e);
        return ApiResponse.error(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "UNSUPPORTED_MEDIA_TYPE",
                "Media type " + e.getContentType() + " is not supported"
        );
    }

    // 处理文件上传大小超限异常
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.error("File size exceeded", e);
        return ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "MAX_UPLOAD_SIZE_EXCEEDED",
                "File size exceeds maximum allowed upload size"
        );
    }

    // 处理所有其他未预期的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred"
        );
    }
    // 处理路径不存在异常
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoHandlerFound(NoHandlerFoundException e) {
        String message = String.format("Could not find the %s method for URL %s",
                e.getHttpMethod(), e.getRequestURL());
        log.warn(message);
        return ApiResponse.error(
                HttpStatus.NOT_FOUND.value(),
                "ENDPOINT_NOT_FOUND",
                message
        );
    }
}