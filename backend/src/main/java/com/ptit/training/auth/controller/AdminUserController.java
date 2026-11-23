package com.ptit.training.auth.controller;

import com.ptit.training.auth.dto.AuthDtos.*;
import com.ptit.training.auth.entity.User;
import com.ptit.training.auth.service.AdminUserService;
import com.ptit.training.common.ApiResponse;
import com.ptit.training.common.PageResponse;
import com.ptit.training.common.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<PageResponse<UserSummaryDto>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) User.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(adminUserService.searchUsers(
                keyword, status, PageRequest.of(page, safeSize, Sort.by("id").ascending())));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ApiResponse<UserSummaryDto> createUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(adminUserService.createUser(principal.getId(), request));
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority('USER_LOCK')")
    public ApiResponse<UserSummaryDto> updateStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return ApiResponse.ok(adminUserService.updateUserStatus(principal.getId(), id, request));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('RBAC_MANAGE')")
    public ApiResponse<List<RoleDetailDto>> listRoles() {
        return ApiResponse.ok(adminUserService.listRoles());
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('RBAC_MANAGE')")
    public ApiResponse<List<PermissionDto>> listPermissions() {
        return ApiResponse.ok(adminUserService.listPermissions());
    }

    @PutMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('RBAC_MANAGE')")
    public ApiResponse<RoleDetailDto> updateRolePermissions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRolePermissionsRequest request) {
        return ApiResponse.ok(adminUserService.updateRolePermissions(principal.getId(), id, request));
    }
}
