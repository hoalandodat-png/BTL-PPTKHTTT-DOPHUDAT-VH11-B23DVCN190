package com.ptit.training.auth.service;

import com.ptit.training.auth.dto.AuthDtos.*;
import com.ptit.training.auth.entity.Permission;
import com.ptit.training.auth.entity.Role;
import com.ptit.training.auth.entity.User;
import com.ptit.training.auth.repository.PermissionRepository;
import com.ptit.training.auth.repository.RefreshTokenRepository;
import com.ptit.training.auth.repository.RoleRepository;
import com.ptit.training.auth.repository.UserRepository;
import com.ptit.training.common.PageResponse;
import com.ptit.training.common.exception.BusinessException;
import com.ptit.training.common.exception.ConflictException;
import com.ptit.training.common.exception.ResourceNotFoundException;
import com.ptit.training.reporting.service.AuditLogService;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dịch vụ quản trị tài khoản, khóa/mở khóa và phân quyền RBAC dành cho Admin.
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AdminUserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryDto> searchUsers(String keyword, User.Status status, Pageable pageable) {
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<User> page = userRepository.searchUsers(cleanKeyword, status, pageable);
        return PageResponse.from(page.map(this::toSummaryDto));
    }

    @Transactional
    public UserSummaryDto createUser(Long actorUserId, CreateUserRequest req) {
        if (userRepository.existsByUsernameIgnoreCase(req.username().trim())) {
            throw new ConflictException("USERNAME_EXISTS", "Tên đăng nhập đã tồn tại: " + req.username());
        }
        if (userRepository.existsByEmailIgnoreCase(req.email().trim())) {
            throw new ConflictException("EMAIL_EXISTS", "Email đã tồn tại: " + req.email());
        }

        Set<Role> roles = new HashSet<>();
        for (String roleCode : req.roleCodes()) {
            Role role = roleRepository.findByCode(roleCode.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("ROLE_NOT_FOUND", "Không tìm thấy vai trò: " + roleCode));
            roles.add(role);
        }

        User user = new User();
        user.setUsername(req.username().trim());
        user.setEmail(req.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setStatus(User.Status.ACTIVE);
        user.setRoles(roles);

        User saved = userRepository.save(user);
        auditLogService.record(actorUserId, "CREATE_USER", "USER", String.valueOf(saved.getId()),
                null, Map.of("username", saved.getUsername(), "roles", req.roleCodes()), "Tạo tài khoản mới");
        return toSummaryDto(saved);
    }

    @Transactional
    public UserSummaryDto updateUserStatus(Long actorUserId, Long targetUserId, UpdateUserStatusRequest req) {
        if (actorUserId.equals(targetUserId) && req.status() != User.Status.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "CANNOT_LOCK_SELF",
                    "Quản trị viên không thể tự khóa tài khoản đang đăng nhập của chính mình");
        }

        User target = userRepository.findByIdWithRolesAndPermissions(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Không tìm thấy tài khoản"));

        User.Status oldStatus = target.getStatus();
        target.setStatus(req.status());
        User saved = userRepository.save(target);

        if (req.status() != User.Status.ACTIVE) {
            refreshTokenRepository.revokeAllActiveByUserId(targetUserId, Instant.now(), "ACCOUNT_LOCKED");
        }

        auditLogService.record(actorUserId,
                req.status() == User.Status.ACTIVE ? "UNLOCK_USER" : "LOCK_USER",
                "USER", String.valueOf(targetUserId),
                Map.of("status", oldStatus.name()),
                Map.of("status", req.status().name()),
                req.reason());

        return toSummaryDto(saved);
    }

    @Transactional(readOnly = true)
    public List<RoleDetailDto> listRoles() {
        return roleRepository.findAllByOrderByIdAsc().stream()
                .map(this::toRoleDetailDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionDto> listPermissions() {
        return permissionRepository.findAllByOrderByModuleAscIdAsc().stream()
                .map(this::toPermissionDto)
                .toList();
    }

    @Transactional
    public RoleDetailDto updateRolePermissions(Long actorUserId, Long roleId, UpdateRolePermissionsRequest req) {
        Role role = roleRepository.findWithPermissionsById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_NOT_FOUND", "Không tìm thấy vai trò"));

        List<String> oldPermCodes = role.getPermissions().stream().map(Permission::getCode).sorted().toList();
        List<Permission> newPerms = permissionRepository.findAllById(req.permissionIds());
        if (newPerms.size() != new HashSet<>(req.permissionIds()).size()) {
            throw new ResourceNotFoundException("PERMISSION_NOT_FOUND", "Có mã quyền hạn không tồn tại");
        }

        role.setPermissions(new HashSet<>(newPerms));
        Role saved = roleRepository.save(role);
        List<String> newPermCodes = saved.getPermissions().stream().map(Permission::getCode).sorted().toList();

        auditLogService.record(actorUserId, "UPDATE_ROLE_PERMISSIONS", "ROLE", String.valueOf(roleId),
                Map.of("permissions", oldPermCodes),
                Map.of("permissions", newPermCodes),
                "Cập nhật quyền hạn cho vai trò " + role.getCode());

        return toRoleDetailDto(saved);
    }

    private UserSummaryDto toSummaryDto(User u) {
        Set<String> roleCodes = u.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new UserSummaryDto(u.getId(), u.getUsername(), u.getEmail(), u.getStatus().name(),
                roleCodes, u.getLastLoginAt(), u.getCreatedAt());
    }

    private RoleDetailDto toRoleDetailDto(Role r) {
        List<PermissionDto> perms = r.getPermissions().stream()
                .map(this::toPermissionDto)
                .sorted((a, b) -> a.module().compareTo(b.module()) != 0
                        ? a.module().compareTo(b.module())
                        : a.code().compareTo(b.code()))
                .toList();
        return new RoleDetailDto(r.getId(), r.getCode(), r.getName(), r.getDescription(), perms);
    }

    private PermissionDto toPermissionDto(Permission p) {
        return new PermissionDto(p.getId(), p.getCode(), p.getName(), p.getModule(), p.getDescription());
    }
}
