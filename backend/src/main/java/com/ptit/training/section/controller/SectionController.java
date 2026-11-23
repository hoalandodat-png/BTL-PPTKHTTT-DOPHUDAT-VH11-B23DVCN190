package com.ptit.training.section.controller;

import com.ptit.training.common.ApiResponse;
import com.ptit.training.common.PageResponse;
import com.ptit.training.common.security.UserPrincipal;
import com.ptit.training.section.dto.SectionDtos.*;
import com.ptit.training.section.service.SectionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping("/academic-years")
    @PreAuthorize("hasAuthority('SECTION_READ')")
    public ApiResponse<List<AcademicYearDto>> listAcademicYears() {
        return ApiResponse.ok(sectionService.listAcademicYears());
    }

    @PostMapping("/academic-years")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SEMESTER_WRITE')")
    public ApiResponse<AcademicYearDto> createAcademicYear(@Valid @RequestBody CreateAcademicYearRequest req) {
        return ApiResponse.ok(sectionService.createAcademicYear(req));
    }

    @GetMapping("/semesters")
    @PreAuthorize("hasAuthority('SECTION_READ')")
    public ApiResponse<List<SemesterDto>> listSemesters() {
        return ApiResponse.ok(sectionService.listSemesters());
    }

    @PostMapping("/semesters")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SEMESTER_WRITE')")
    public ApiResponse<SemesterDto> createSemester(@Valid @RequestBody CreateSemesterRequest req) {
        return ApiResponse.ok(sectionService.createSemester(req));
    }

    @PatchMapping("/semesters/{id}/registration-period")
    @PreAuthorize("hasAuthority('SEMESTER_WRITE')")
    public ApiResponse<SemesterDto> updateRegistrationPeriod(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRegistrationPeriodRequest req) {
        return ApiResponse.ok(sectionService.updateRegistrationPeriod(principal.getId(), id, req));
    }

    @GetMapping("/classrooms")
    @PreAuthorize("hasAuthority('SECTION_READ')")
    public ApiResponse<List<ClassroomDto>> listClassrooms() {
        return ApiResponse.ok(sectionService.listClassrooms());
    }

    @PostMapping("/classrooms")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SECTION_WRITE')")
    public ApiResponse<ClassroomDto> createClassroom(@Valid @RequestBody CreateClassroomRequest req) {
        return ApiResponse.ok(sectionService.createClassroom(req));
    }

    @GetMapping("/sections")
    @PreAuthorize("hasAuthority('SECTION_READ')")
    public ApiResponse<PageResponse<CourseSectionDto>> searchSections(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long lecturerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(sectionService.searchSections(
                semesterId, courseId, lecturerId, keyword,
                PageRequest.of(page, safeSize, Sort.by("sectionCode").ascending())));
    }

    @GetMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('SECTION_READ')")
    public ApiResponse<CourseSectionDto> getSection(@PathVariable Long id) {
        return ApiResponse.ok(sectionService.getSectionDetail(id));
    }

    @PostMapping("/sections")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SECTION_WRITE')")
    public ApiResponse<CourseSectionDto> createSection(@Valid @RequestBody CreateCourseSectionRequest req) {
        return ApiResponse.ok(sectionService.createCourseSection(req));
    }

    @PutMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('SECTION_WRITE')")
    public ApiResponse<CourseSectionDto> updateSection(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseSectionRequest req) {
        return ApiResponse.ok(sectionService.updateCourseSection(id, req));
    }

    @GetMapping("/sections/{id}/students")
    @PreAuthorize("hasAnyAuthority('SECTION_WRITE', 'GRADE_WRITE')")
    public ApiResponse<List<SectionStudentRosterDto>> getSectionStudents(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ApiResponse.ok(sectionService.getSectionStudents(principal, id));
    }

    @GetMapping("/schedules/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PersonalTimetableDto> getMyTimetable(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Long semesterId,
            @RequestParam(required = false) Integer weekNumber) {
        return ApiResponse.ok(sectionService.getPersonalTimetable(principal, semesterId, weekNumber));
    }
}
