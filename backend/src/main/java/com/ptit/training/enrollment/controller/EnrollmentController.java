package com.ptit.training.enrollment.controller;

import com.ptit.training.common.ApiResponse;
import com.ptit.training.common.security.UserPrincipal;
import com.ptit.training.enrollment.dto.EnrollmentDtos.*;
import com.ptit.training.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/enrollments/me")
    @PreAuthorize("hasAuthority('ENROLLMENT_SELF')")
    public ApiResponse<StudentSemesterEnrollmentDto> getMyEnrollments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Long semesterId) {
        return ApiResponse.ok(enrollmentService.getMyEnrollments(principal.getId(), semesterId));
    }

    @PostMapping("/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ENROLLMENT_SELF')")
    public ApiResponse<EnrollmentResponseDto> enroll(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody EnrollRequest request) {
        return ApiResponse.ok(enrollmentService.enrollStudent(principal.getId(), request.sectionId()));
    }

    @DeleteMapping("/enrollments/{id}")
    @PreAuthorize("hasAuthority('ENROLLMENT_SELF')")
    public ApiResponse<Map<String, String>> cancelMyEnrollment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        enrollmentService.cancelStudentEnrollment(principal.getId(), id);
        return ApiResponse.ok(Map.of("message", "Đã hủy đăng ký học phần thành công"));
    }

    @PostMapping("/enrollments/override")
    @PreAuthorize("hasAuthority('ENROLLMENT_OVERRIDE')")
    public ApiResponse<EnrollmentResponseDto> overrideEnrollment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OverrideEnrollRequest request) {
        return ApiResponse.ok(enrollmentService.overrideEnrollment(principal.getId(), request));
    }

    @GetMapping("/students/me/curriculum-progress")
    @PreAuthorize("hasAuthority('RESULT_READ_SELF')")
    public ApiResponse<CurriculumProgressDto> getMyCurriculumProgress(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(enrollmentService.getCurriculumProgress(principal.getId()));
    }
}
