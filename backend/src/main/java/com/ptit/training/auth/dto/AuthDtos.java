package com.ptit.training.auth.dto;

import com.ptit.training.auth.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public final class AuthDtos {

    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank(message = "Vui lòng nhập tên đăng nhập hoặc email") String usernameOrEmail,
            @NotBlank(message = "Vui lòng nhập mật khẩu") String password
    ) {}

    public record ChangePasswordRequest(
            @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại") String currentPassword,
            @NotBlank @Size(min = 8, message = "Mật khẩu mới phải từ 8 ký tự trở lên") String newPassword,
            @NotBlank(message = "Vui lòng xác nhận mật khẩu mới") String confirmPassword
    ) {}

    public record ForgotPasswordRequest(
            @NotBlank @Email(message = "Email không đúng định dạng") String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank(message = "Thiếu mã đặt lại mật khẩu") String token,
            @NotBlank @Size(min = 8, message = "Mật khẩu mới phải từ 8 ký tự trở lên") String newPassword
    ) {}

    public record UserProfileDto(
            Long id,
            String username,
            String email,
            String status,
            Set<String> roles,
            Set<String> permissions,
            String redirectPath
    ) {}

    public record UserSummaryDto(
            Long id,
            String username,
            String email,
            String status,
            Set<String> roles,
            Instant lastLoginAt,
            Instant createdAt
    ) {}

    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            @NotEmpty List<String> roleCodes
    ) {}

    public record UpdateUserStatusRequest(
            @NotNull User.Status status,
            @NotBlank(message = "Vui lòng nhập lý do thay đổi trạng thái") String reason
    ) {}

    public record PermissionDto(
            Long id,
            String code,
            String name,
            String module,
            String description
    ) {}

    public record RoleDetailDto(
            Long id,
            String code,
            String name,
            String description,
            List<PermissionDto> permissions
    ) {}

    public record UpdateRolePermissionsRequest(
            @NotEmpty(message = "Danh sách quyền không được rỗng") List<Long> permissionIds
    ) {}
}
