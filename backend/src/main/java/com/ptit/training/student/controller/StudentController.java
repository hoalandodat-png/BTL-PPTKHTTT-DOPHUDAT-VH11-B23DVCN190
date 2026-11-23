package com.ptit.training.student.controller;

import com.ptit.training.common.ApiResponse;
import com.ptit.training.common.PageResponse;
import com.ptit.training.common.security.UserPrincipal;
import com.ptit.training.student.dto.StudentDtos.*;
import com.ptit.training.student.entity.Student;
import com.ptit.training.student.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT_READ')")
    public ApiResponse<PageResponse<StudentSummaryDto>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Long academicClassId,
            @RequestParam(required = false) Integer cohortYear,
            @RequestParam(required = false) Student.AcademicStatus academicStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(studentService.searchStudents(
                keyword, majorId, academicClassId, cohortYear, academicStatus,
                PageRequest.of(page, safeSize, Sort.by("studentCode").ascending())));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('STUDENT_SELF_UPDATE')")
    public ApiResponse<StudentDetailDto> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(studentService.getMyProfile(principal.getId()));
    }

    @PatchMapping("/me/contact")
    @PreAuthorize("hasAuthority('STUDENT_SELF_UPDATE')")
    public ApiResponse<StudentDetailDto> updateMyContact(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateSelfContactRequest request) {
        return ApiResponse.ok(studentService.updateSelfContact(principal.getId(), request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_READ')")
    public ApiResponse<StudentDetailDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(studentService.getStudentById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('STUDENT_WRITE')")
    public ApiResponse<StudentDetailDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateStudentRequest request) {
        return ApiResponse.ok(studentService.createStudent(principal.getId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_WRITE')")
    public ApiResponse<StudentDetailDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentRequest request) {
        return ApiResponse.ok(studentService.updateStudent(id, request));
    }

    @PatchMapping("/{id}/academic-status")
    @PreAuthorize("hasAuthority('STUDENT_WRITE')")
    public ApiResponse<StudentDetailDto> updateStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAcademicStatusRequest request) {
        return ApiResponse.ok(studentService.updateAcademicStatus(principal.getId(), id, request));
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('STUDENT_WRITE')")
    public ApiResponse<StudentImportResultDto> importCsv(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean dryRun) {
        return ApiResponse.ok(studentService.importStudentsCsv(principal.getId(), file, dryRun));
    }
}
