package com.ptit.training.section.controller;

import com.ptit.training.common.ApiResponse;
import com.ptit.training.common.security.UserPrincipal;
import com.ptit.training.section.dto.ExamDtos.*;
import com.ptit.training.section.service.ExamScheduleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exams")
public class ExamController {

    private final ExamScheduleService examService;

    public ExamController(ExamScheduleService examService) {
        this.examService = examService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('RESULT_READ_SELF')")
    public ApiResponse<List<ExamScheduleDto>> getMyExams(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Long semesterId) {
        return ApiResponse.ok(examService.getMyExams(principal, semesterId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SECTION_READ')")
    public ApiResponse<List<ExamScheduleDto>> listBySemester(@RequestParam Long semesterId) {
        return ApiResponse.ok(examService.listBySemester(semesterId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SECTION_WRITE')")
    public ApiResponse<ExamScheduleDto> createExam(@Valid @RequestBody CreateExamScheduleRequest req) {
        return ApiResponse.ok(examService.createExam(req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SECTION_WRITE')")
    public ApiResponse<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ApiResponse.ok(null);
    }
}
