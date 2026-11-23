package com.ptit.training.organization.controller;

import com.ptit.training.common.ApiResponse;
import com.ptit.training.common.PageResponse;
import com.ptit.training.organization.dto.OrganizationDtos.*;
import com.ptit.training.organization.entity.Faculty;
import com.ptit.training.organization.service.OrganizationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class OrganizationController {

    private final OrganizationService orgService;

    public OrganizationController(OrganizationService orgService) {
        this.orgService = orgService;
    }

    // --- FACULTIES ---
    @GetMapping("/faculties")
    @PreAuthorize("hasAuthority('ORG_READ')")
    public ApiResponse<List<FacultyDto>> listFaculties(@RequestParam(required = false) Faculty.Status status) {
        return ApiResponse.ok(orgService.listFaculties(status));
    }

    @PostMapping("/faculties")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    public ApiResponse<FacultyDto> createFaculty(@Valid @RequestBody CreateFacultyRequest req) {
        return ApiResponse.ok(orgService.createFaculty(req));
    }

    @PutMapping("/faculties/{id}")
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    public ApiResponse<FacultyDto> updateFaculty(@PathVariable Long id, @Valid @RequestBody UpdateFacultyRequest req) {
        return ApiResponse.ok(orgService.updateFaculty(id, req));
    }

    // --- DEPARTMENTS ---
    @GetMapping("/departments")
    @PreAuthorize("hasAuthority('ORG_READ')")
    public ApiResponse<List<DepartmentDto>> listDepartments(@RequestParam(required = false) Long facultyId) {
        return ApiResponse.ok(orgService.listDepartments(facultyId));
    }

    @PostMapping("/departments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    public ApiResponse<DepartmentDto> createDepartment(@Valid @RequestBody CreateDepartmentRequest req) {
        return ApiResponse.ok(orgService.createDepartment(req));
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    public ApiResponse<DepartmentDto> updateDepartment(@PathVariable Long id, @Valid @RequestBody UpdateDepartmentRequest req) {
        return ApiResponse.ok(orgService.updateDepartment(id, req));
    }

    // --- MAJORS ---
    @GetMapping("/majors")
    @PreAuthorize("hasAuthority('ORG_READ')")
    public ApiResponse<List<MajorDto>> listMajors(@RequestParam(required = false) Long facultyId) {
        return ApiResponse.ok(orgService.listMajors(facultyId));
    }

    @PostMapping("/majors")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    public ApiResponse<MajorDto> createMajor(@Valid @RequestBody CreateMajorRequest req) {
        return ApiResponse.ok(orgService.createMajor(req));
    }

    @PutMapping("/majors/{id}")
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    public ApiResponse<MajorDto> updateMajor(@PathVariable Long id, @Valid @RequestBody UpdateMajorRequest req) {
        return ApiResponse.ok(orgService.updateMajor(id, req));
    }

    // --- ACADEMIC CLASSES ---
    @GetMapping("/academic-classes")
    @PreAuthorize("hasAuthority('ORG_READ')")
    public ApiResponse<List<AcademicClassDto>> listAcademicClasses(
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Integer cohortYear) {
        return ApiResponse.ok(orgService.listAcademicClasses(majorId, cohortYear));
    }

    @PostMapping("/academic-classes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    public ApiResponse<AcademicClassDto> createAcademicClass(@Valid @RequestBody CreateAcademicClassRequest req) {
        return ApiResponse.ok(orgService.createAcademicClass(req));
    }

    @PutMapping("/academic-classes/{id}")
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    public ApiResponse<AcademicClassDto> updateAcademicClass(
            @PathVariable Long id, @Valid @RequestBody UpdateAcademicClassRequest req) {
        return ApiResponse.ok(orgService.updateAcademicClass(id, req));
    }

    // --- LECTURERS ---
    @GetMapping("/lecturers")
    @PreAuthorize("hasAuthority('ORG_READ')")
    public ApiResponse<PageResponse<LecturerDto>> searchLecturers(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(orgService.searchLecturers(
                departmentId, keyword, PageRequest.of(page, safeSize, Sort.by("lecturerCode").ascending())));
    }

    @PostMapping("/lecturers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    public ApiResponse<LecturerDto> createLecturer(@Valid @RequestBody CreateLecturerRequest req) {
        return ApiResponse.ok(orgService.createLecturer(req));
    }

    @PutMapping("/lecturers/{id}")
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    public ApiResponse<LecturerDto> updateLecturer(@PathVariable Long id, @Valid @RequestBody UpdateLecturerRequest req) {
        return ApiResponse.ok(orgService.updateLecturer(id, req));
    }
}
