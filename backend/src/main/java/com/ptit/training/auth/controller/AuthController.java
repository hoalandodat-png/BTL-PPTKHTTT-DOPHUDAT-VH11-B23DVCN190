package com.ptit.training.auth.controller;

import com.ptit.training.auth.dto.AuthDtos.*;
import com.ptit.training.auth.service.AuthService;
import com.ptit.training.common.ApiResponse;
import com.ptit.training.common.security.JwtAuthenticationFilter;
import com.ptit.training.common.security.JwtTokenProvider;
import com.ptit.training.common.security.UserPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;
    private final boolean cookieSecure;

    public AuthController(
            AuthService authService,
            JwtTokenProvider tokenProvider,
            @Value("${app.jwt.cookie-secure:false}") boolean cookieSecure) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/login")
    public ApiResponse<UserProfileDto> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpReq,
            HttpServletResponse httpRes) {
        AuthService.AuthTokensResult result = authService.login(
                request, httpReq.getRemoteAddr(), httpReq.getHeader("User-Agent"));
        attachAuthCookies(httpRes, result.accessToken(), result.refreshToken());
        return ApiResponse.ok(result.profile());
    }

    @PostMapping("/refresh")
    public ApiResponse<UserProfileDto> refresh(HttpServletRequest httpReq, HttpServletResponse httpRes) {
        String rawRefresh = readCookie(httpReq, JwtAuthenticationFilter.REFRESH_COOKIE_NAME);
        AuthService.AuthTokensResult result = authService.refresh(
                rawRefresh, httpReq.getRemoteAddr(), httpReq.getHeader("User-Agent"));
        attachAuthCookies(httpRes, result.accessToken(), result.refreshToken());
        return ApiResponse.ok(result.profile());
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, String>> logout(HttpServletRequest httpReq, HttpServletResponse httpRes) {
        String rawRefresh = readCookie(httpReq, JwtAuthenticationFilter.REFRESH_COOKIE_NAME);
        authService.logout(rawRefresh);
        clearAuthCookies(httpRes);
        return ApiResponse.ok(Map.of("message", "Đã đăng xuất thành công"));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileDto> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(authService.getCurrentProfile(principal.getId()));
    }

    @PutMapping("/change-password")
    public ApiResponse<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request);
        return ApiResponse.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.ok(Map.of("message",
                "Nếu email tồn tại trong hệ thống, liên kết đặt lại mật khẩu đã được gửi"));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok(Map.of("message", "Đặt lại mật khẩu thành công, vui lòng đăng nhập lại"));
    }

    private void attachAuthCookies(HttpServletResponse res, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from(JwtAuthenticationFilter.ACCESS_COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(tokenProvider.getAccessExpirationMs()))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(JwtAuthenticationFilter.REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofMillis(tokenProvider.getRefreshExpirationMs()))
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        res.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private void clearAuthCookies(HttpServletResponse res) {
        ResponseCookie accessCookie = ResponseCookie.from(JwtAuthenticationFilter.ACCESS_COOKIE_NAME, "")
                .httpOnly(true).secure(cookieSecure).sameSite("Lax").path("/").maxAge(0).build();
        ResponseCookie refreshCookie = ResponseCookie.from(JwtAuthenticationFilter.REFRESH_COOKIE_NAME, "")
                .httpOnly(true).secure(cookieSecure).sameSite("Lax").path("/api/v1/auth").maxAge(0).build();
        res.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        res.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private String readCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
