package com.taoziyoyo.files.model;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private boolean success;
    private int code;
    private String errorCode;
    private String message;
    private T data;

//
//    public static <T> ApiResponse<T> success(T data) {
//        return new ApiResponse<>(200, "success", data);
//    }
//
//    public static <T> ApiResponse<T> error(int code, String message) {
//        return new ApiResponse<>(code, message, null);
//    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, null, "success", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return error(code, null, message);
    }

    public static <T> ApiResponse<T> error(int code, String errorCode, String message) {
        return new ApiResponse<>(false, code, errorCode, message, null);
    }
}
