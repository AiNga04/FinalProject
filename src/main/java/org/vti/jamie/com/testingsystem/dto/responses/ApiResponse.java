package org.vti.jamie.com.testingsystem.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

public record ApiResponse<T>(
        int result,
        long time,
        String dataDescription,
        T data,
        Object data2nd,
        ErrorInfo error
) {

    public record ErrorInfo(
            int code,
            String message,

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            Map<String, String> details
    ) {
    }

    public static <T> ApiResponse<T> success(
            int status,
            String message,
            T data
    ) {
        return success(status, message, data, null);
    }

    public static <T> ApiResponse<T> success(
            int status,
            String message,
            T data,
            Object data2nd
    ) {
        return new ApiResponse<>(
                1,
                Instant.now().getEpochSecond(),
                "",
                data,
                data2nd,
                new ErrorInfo(
                        status,
                        message,
                        Map.of()
                )
        );
    }

    public static ApiResponse<Void> error(
            int status,
            String message,
            Map<String, String> details
    ) {
        return new ApiResponse<>(
                0,
                Instant.now().getEpochSecond(),
                "",
                null,
                null,
                new ErrorInfo(
                        status,
                        message,
                        Map.copyOf(details)
                )
        );
    }
}
