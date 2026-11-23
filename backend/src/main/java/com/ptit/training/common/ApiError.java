package com.ptit.training.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Mô tả lỗi chi tiết kèm mã lỗi nghiệp vụ phục vụ xử lý ở Frontend.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String code,
        String message,
        List<FieldIssue> details
) {
    public record FieldIssue(String field, String issue) {}

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null);
    }

    public static ApiError of(String code, String message, List<FieldIssue> details) {
        return new ApiError(code, message, details);
    }
}
