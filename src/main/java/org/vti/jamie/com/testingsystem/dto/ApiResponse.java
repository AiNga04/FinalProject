package org.vti.jamie.com.testingsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String message,
        T data,
        Map<String, String> errors,
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant timestamp
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return success(message, data);
    }

    public static <T> ApiResponse<T> success(int status, String message, T data, Object unused) {
        return success(message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("Thành công", data);
    }

    public static ApiResponse<Void> error(String message, Map<String, String> errors) {
        return new ApiResponse<>(
                message,
                null,
                errors == null || errors.isEmpty() ? null : Map.copyOf(errors),
                Instant.now()
        );
    }

    public static ApiResponse<Void> error(int status, String message, Map<String, String> errors) {
        return error(message, errors);
    }

    public static ApiResponse<Void> error(String message) {
        return error(message, null);
    }
}
