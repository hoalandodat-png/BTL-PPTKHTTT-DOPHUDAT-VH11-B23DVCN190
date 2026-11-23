package com.ptit.training.course.controller;

import com.ptit.training.common.ApiResponse;
import com.ptit.training.common.PageResponse;
import com.ptit.training.course.dto.CourseDtos.*;
import com.ptit.training.course.service.CourseService;
import com.ptit.training.organization.entity.Faculty;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/courses")
    @PreAuthorize("hasAuthority('COURSE_READ')")
    public ApiResponse<PageResponse<CourseDto>> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Faculty.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ApiResponse.ok(courseService.searchCourses(
                keyword, departmentId, status, PageRequest.of(page, safeSize, Sort.by("code").ascending())));
    }

    @GetMapping("/courses/{id}")
    @PreAuthorize("hasAuthority('COURSE_READ')")
    public ApiResponse<CourseDetailDto> getCourse(@PathVariable Long id) {
        return ApiResponse.ok(courseService.getCourseDetail(id));
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('COURSE_WRITE')")
    public ApiResponse<CourseDetailDto> createCourse(@Valid @RequestBody CreateCourseRequest req) {
        return ApiResponse.ok(courseService.createCourse(req));
    }

    @PutMapping("/courses/{id}")
    @PreAuthorize("hasAuthority('COURSE_WRITE')")
    public ApiResponse<CourseDetailDto> updateCourse(@PathVariable Long id, @Valid @RequestBody UpdateCourseRequest req) {
        return ApiResponse.ok(courseService.updateCourse(id, req));
    }

    @PutMapping("/courses/{id}/prerequisites")
    @PreAuthorize("hasAuthority('COURSE_WRITE')")
    public ApiResponse<List<PrerequisiteDto>> updatePrerequisites(
            @PathVariable Long id, @Valid @RequestBody UpdatePrerequisitesRequest req) {
        return ApiResponse.ok(courseService.updatePrerequisites(id, req));
    }

    @GetMapping("/curriculums")
    @PreAuthorize("hasAuthority('COURSE_READ')")
    public ApiResponse<List<CurriculumDto>> listCurriculums(
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Integer cohortYear) {
        return ApiResponse.ok(courseService.listCurriculums(majorId, cohortYear));
    }

    @GetMapping("/curriculums/{id}")
    @PreAuthorize("hasAuthority('COURSE_READ')")
    public ApiResponse<CurriculumDetailDto> getCurriculum(@PathVariable Long id) {
        return ApiResponse.ok(courseService.getCurriculumDetail(id));
    }

    @PostMapping("/curriculums")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('COURSE_WRITE')")
    public ApiResponse<CurriculumDetailDto> createCurriculum(@Valid @RequestBody CreateCurriculumRequest req) {
        return ApiResponse.ok(courseService.createCurriculum(req));
    }
}
