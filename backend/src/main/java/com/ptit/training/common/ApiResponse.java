package com.ptit.training.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Chuẩn hóa cấu trúc phản hồi JSON cho toàn bộ REST API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    public static ApiResponse<Void> okMessage(String message) {
        return new ApiResponse<>(true, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> fail(ApiError error) {
        return new ApiResponse<>(false, null, error, Instant.now());
    }
}
