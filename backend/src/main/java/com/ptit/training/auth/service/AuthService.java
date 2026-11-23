package com.ptit.training.auth.service;

import com.ptit.training.auth.dto.AuthDtos.*;
import com.ptit.training.auth.entity.Permission;
import com.ptit.training.auth.entity.RefreshToken;
import com.ptit.training.auth.entity.Role;
import com.ptit.training.auth.entity.User;
import com.ptit.training.auth.repository.RefreshTokenRepository;
import com.ptit.training.auth.repository.UserRepository;
import com.ptit.training.common.exception.BusinessException;
import com.ptit.training.common.security.JwtTokenProvider;
import com.ptit.training.common.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xử lý xác thực, phát hành JWT + Refresh Token Rotation, đổi và đặt lại mật khẩu.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final String mailFrom;
    private final String frontendUrl;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            @Value("${app.mail.from:noreply@ptit-demo.edu.vn}") String mailFrom,
            @Value("${app.mail.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.mailFrom = mailFrom;
        this.frontendUrl = frontendUrl;
    }

    public record AuthTokensResult(UserProfileDto profile, String accessToken, String refreshToken) {}

    @Transactional
    public AuthTokensResult login(LoginRequest req, String ipAddress, String userAgent) {
        User user = userRepository.findByUsernameOrEmailWithRolesAndPermissions(req.usernameOrEmail().trim())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS",
                        "Tài khoản hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS",
                    "Tài khoản hoặc mật khẩu không chính xác");
        }

        if (!user.isActive()) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_LOCKED",
                    "Tài khoản của bạn đã bị khóa hoặc ngưng hoạt động");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        return issueTokensForUser(user, ipAddress, userAgent);
    }

    @Transactional
    public AuthTokensResult refresh(String rawRefreshToken, String ipAddress, String userAgent) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_INVALID",
                    "Phiên làm việc đã hết hạn, vui lòng đăng nhập lại");
        }

        String tokenHash = tokenProvider.sha256Hex(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHashWithUser(tokenHash)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_INVALID",
                        "Refresh token không hợp lệ"));

        Instant now = Instant.now();
        User user = userRepository.findByIdWithRolesAndPermissions(existing.getUser().getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_INVALID",
                        "Tài khoản không tồn tại"));

        if (!user.isActive()) {
            refreshTokenRepository.revokeAllActiveByUserId(user.getId(), now, "ACCOUNT_LOCKED");
            throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_LOCKED",
                    "Tài khoản của bạn đã bị khóa");
        }

        if (!existing.isValidAt(now)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_INVALID",
                    "Phiên làm việc đã hết hạn hoặc bị thu hồi");
        }

        existing.setRevokedAt(now);
        existing.setRevokedReason("TOKEN_ROTATED");
        refreshTokenRepository.save(existing);

        return issueTokensForUser(user, ipAddress, userAgent);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        String tokenHash = tokenProvider.sha256Hex(rawRefreshToken);
        refreshTokenRepository.findByTokenHashWithUser(tokenHash).ifPresent(rt -> {
            if (rt.getRevokedAt() == null) {
                rt.setRevokedAt(Instant.now());
                rt.setRevokedReason("USER_LOGOUT");
                refreshTokenRepository.save(rt);
            }
        });
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PASSWORD_CONFIRM_MISMATCH",
                    "Xác nhận mật khẩu mới không khớp");
        }
        if (req.currentPassword().equals(req.newPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PASSWORD_UNCHANGED",
                    "Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "AUTH_WRONG_CURRENT_PASSWORD",
                    "Mật khẩu hiện tại không chính xác");
        }

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllActiveByUserId(userId, Instant.now(), "PASSWORD_CHANGED");
    }

    public void forgotPassword(ForgotPasswordRequest req) {
        userRepository.findByEmailIgnoreCase(req.email().trim()).ifPresent(user -> {
            if (!user.isActive()) return;
            String fingerprint = user.getPasswordHash().substring(Math.max(0, user.getPasswordHash().length() - 12));
            String resetToken = tokenProvider.generatePasswordResetToken(user.getId(), fingerprint);
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
            sendResetEmailSafely(user.getEmail(), user.getUsername(), resetUrl);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        Claims claims;
        try {
            claims = tokenProvider.parsePasswordResetToken(req.token());
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "AUTH_RESET_TOKEN_EXPIRED",
                    "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn");
        }

        Long userId = Long.parseLong(claims.getSubject());
        String fp = claims.get("fp", String.class);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "AUTH_RESET_TOKEN_EXPIRED",
                        "Liên kết đặt lại mật khẩu không hợp lệ"));

        String currentFp = user.getPasswordHash().substring(Math.max(0, user.getPasswordHash().length() - 12));
        if (!currentFp.equals(fp)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "AUTH_RESET_TOKEN_EXPIRED",
                    "Liên kết đặt lại mật khẩu đã được sử dụng");
        }

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllActiveByUserId(userId, Instant.now(), "PASSWORD_RESET");
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentProfile(Long userId) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Tài khoản không tồn tại"));
        if (!user.isActive()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_LOCKED", "Tài khoản đã bị khóa");
        }
        return toProfileDto(user);
    }

    private AuthTokensResult issueTokensForUser(User user, String ipAddress, String userAgent) {
        UserPrincipal principal = toPrincipal(user);
        String accessToken = tokenProvider.generateAccessToken(principal);
        String rawRefreshToken = tokenProvider.generateOpaqueRefreshToken();

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(tokenProvider.sha256Hex(rawRefreshToken));
        rt.setExpiresAt(Instant.now().plusMillis(tokenProvider.getRefreshExpirationMs()));
        rt.setIpAddress(ipAddress);
        rt.setUserAgent(userAgent != null && userAgent.length() > 255 ? userAgent.substring(0, 255) : userAgent);
        refreshTokenRepository.save(rt);

        return new AuthTokensResult(toProfileDto(user), accessToken, rawRefreshToken);
    }

    private UserPrincipal toPrincipal(User user) {
        Set<String> roleCodes = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> permCodes = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.isActive(),
                roleCodes,
                permCodes
        );
    }

    private UserProfileDto toProfileDto(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus().name(),
                roles,
                permissions,
                resolveRedirectPath(roles)
        );
    }

    private String resolveRedirectPath(Set<String> roles) {
        if (roles.contains("ADMIN")) return "/admin";
        if (roles.contains("ACADEMIC_AFFAIRS")) return "/academic";
        if (roles.contains("LECTURER")) return "/lecturer";
        if (roles.contains("STUDENT")) return "/student";
        return "/";
    }

    private void sendResetEmailSafely(String toEmail, String username, String resetUrl) {
        if (mailSender == null) {
            log.info("[MailMock] Reset password link for {} ({}): {}", username, toEmail, resetUrl);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject("[PTIT Đào tạo] Yêu cầu đặt lại mật khẩu");
            helper.setText("Xin chào " + username + ",\n\nVui lòng truy cập liên kết sau (có hiệu lực 15 phút) để đặt lại mật khẩu:\n" + resetUrl);
            mailSender.send(msg);
        } catch (Exception ex) {
            log.warn("Không gửi được email tới SMTP ({}). Link reset: {}", ex.getMessage(), resetUrl);
        }
    }
}
