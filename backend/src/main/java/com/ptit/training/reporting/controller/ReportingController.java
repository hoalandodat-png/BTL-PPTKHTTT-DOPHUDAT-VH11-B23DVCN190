package com.ptit.training.reporting.controller;

import com.ptit.training.common.ApiResponse;
import com.ptit.training.common.PageResponse;
import com.ptit.training.common.security.UserPrincipal;
import com.ptit.training.reporting.dto.ReportingDtos.*;
import com.ptit.training.reporting.service.ReportingService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/admin/audit-logs")
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    public ApiResponse<PageResponse<AuditLogDto>> searchAuditLogs(
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(reportingService.searchAuditLogs(
                actorUserId, action, entityType, PageRequest.of(page, safeSize)));
    }

    @GetMapping("/reports/student-statistics")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ApiResponse<StudentStatisticsReportDto> studentStatistics(
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Integer cohortYear) {
        return ApiResponse.ok(reportingService.getStudentStatistics(facultyId, cohortYear));
    }

    @GetMapping("/reports/enrollment-capacity")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ApiResponse<SectionCapacityReportDto> enrollmentCapacity(
            @RequestParam(required = false) Long semesterId) {
        return ApiResponse.ok(reportingService.getEnrollmentCapacityReport(semesterId));
    }

    @GetMapping("/reports/grade-distribution")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ApiResponse<GradeDistributionReportDto> gradeDistribution(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long sectionId) {
        return ApiResponse.ok(reportingService.getGradeDistribution(principal, semesterId, courseId, sectionId));
    }
}
