package com.ptit.training.grade.controller;

import com.ptit.training.common.ApiResponse;
import com.ptit.training.common.security.UserPrincipal;
import com.ptit.training.grade.dto.GradeDtos.*;
import com.ptit.training.grade.service.GradeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping("/sections/{sectionId}/grades")
    @PreAuthorize("hasAnyAuthority('GRADE_WRITE', 'GRADE_LOCK')")
    public ApiResponse<SectionGradeBookDto> getSectionGradeBook(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long sectionId) {
        return ApiResponse.ok(gradeService.getSectionGradeBook(principal, sectionId));
    }

    @PutMapping("/sections/{sectionId}/grades")
    @PreAuthorize("hasAuthority('GRADE_WRITE')")
    public ApiResponse<SectionGradeBookDto> saveGrades(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long sectionId,
            @Valid @RequestBody SaveSectionGradesRequest request) {
        return ApiResponse.ok(gradeService.saveSectionGrades(principal, sectionId, request));
    }

    @PostMapping("/sections/{sectionId}/grades/import")
    @PreAuthorize("hasAuthority('GRADE_WRITE')")
    public ApiResponse<ImportGradeResultDto> importGradesCsv(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long sectionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean dryRun) {
        return ApiResponse.ok(gradeService.importSectionGradesCsv(principal, sectionId, file, dryRun));
    }

    @PostMapping("/sections/{sectionId}/grades/publish")
    @PreAuthorize("hasAuthority('GRADE_PUBLISH')")
    public ApiResponse<SectionGradeBookDto> publishGrades(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long sectionId) {
        return ApiResponse.ok(gradeService.publishGrades(principal, sectionId));
    }

    @PostMapping("/sections/{sectionId}/grades/lock")
    @PreAuthorize("hasAuthority('GRADE_LOCK')")
    public ApiResponse<SectionGradeBookDto> lockGrades(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long sectionId) {
        return ApiResponse.ok(gradeService.lockGrades(principal, sectionId));
    }

    @PatchMapping("/enrollments/{enrollmentId}/grades/override")
    @PreAuthorize("hasAuthority('GRADE_LOCK')")
    public ApiResponse<StudentGradeRowDto> overrideScore(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long enrollmentId,
            @Valid @RequestBody OverrideScoreRequest request) {
        return ApiResponse.ok(gradeService.overrideScore(principal, enrollmentId, request));
    }

    @GetMapping("/students/me/academic-results")
    @PreAuthorize("hasAuthority('RESULT_READ_SELF')")
    public ApiResponse<AcademicTranscriptDto> getMyResults(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(gradeService.getMyTranscript(principal.getId()));
    }

    @GetMapping("/students/{studentId}/academic-results")
    @PreAuthorize("hasAuthority('STUDENT_READ')")
    public ApiResponse<AcademicTranscriptDto> getStudentResults(@PathVariable Long studentId) {
        return ApiResponse.ok(gradeService.getStudentTranscript(studentId));
    }

    @GetMapping("/system-configs/grading-scales")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GradingScaleDto>> listGradingScales() {
        return ApiResponse.ok(gradeService.listGradingScales());
    }

    @PutMapping("/system-configs/grading-scales")
    @PreAuthorize("hasAnyAuthority('RBAC_MANAGE', 'SEMESTER_WRITE')")
    public ApiResponse<List<GradingScaleDto>> updateGradingScales(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateGradingScalesRequest request) {
        return ApiResponse.ok(gradeService.updateGradingScales(principal.getId(), request));
    }

    @GetMapping("/system-configs")
    @PreAuthorize("hasAnyAuthority('RBAC_MANAGE', 'SEMESTER_WRITE')")
    public ApiResponse<List<SystemConfigDto>> listConfigs() {
        return ApiResponse.ok(gradeService.listSystemConfigs());
    }

    @PutMapping("/system-configs/{key}")
    @PreAuthorize("hasAnyAuthority('RBAC_MANAGE', 'SEMESTER_WRITE')")
    public ApiResponse<SystemConfigDto> updateConfig(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        return ApiResponse.ok(gradeService.updateSystemConfig(principal.getId(), key, body.getOrDefault("configValue", "")));
    }
}
