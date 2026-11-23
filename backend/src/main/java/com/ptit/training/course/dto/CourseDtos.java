package com.ptit.training.course.dto;

import com.ptit.training.course.entity.CoursePrerequisite;
import com.ptit.training.organization.entity.Faculty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public final class CourseDtos {

    private CourseDtos() {}

    public record GradeComponentDto(
            Long id,
            @NotBlank @Size(max = 30) String code,
            @NotBlank @Size(max = 100) String name,
            @NotNull @Min(1) @Max(100) Integer weightPercent,
            @NotNull Boolean isFinalExam,
            @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal minThreshold,
            @NotNull @Min(1) Integer displayOrder
    ) {}

    public record PrerequisiteDto(
            Long id,
            Long prerequisiteCourseId,
            String prerequisiteCourseCode,
            String prerequisiteCourseName,
            Integer prerequisiteCourseCredits,
            String conditionType
    ) {}

    public record CourseDto(
            Long id,
            String code,
            String name,
            Integer credits,
            Integer theoryHours,
            Integer practiceHours,
            Long departmentId,
            String departmentName,
            String facultyName,
            String status
    ) {}

    public record CourseDetailDto(
            Long id,
            String code,
            String name,
            Integer credits,
            Integer theoryHours,
            Integer practiceHours,
            Long departmentId,
            String departmentName,
            String status,
            List<GradeComponentDto> gradeComponents,
            List<PrerequisiteDto> prerequisites
    ) {}

    public record CreateCourseRequest(
            @NotBlank @Size(max = 20) String code,
            @NotBlank @Size(max = 150) String name,
            @NotNull @Min(1) @Max(15) Integer credits,
            @NotNull @Min(0) Integer theoryHours,
            @NotNull @Min(0) Integer practiceHours,
            @NotNull Long departmentId,
            @NotEmpty @Valid List<GradeComponentDto> gradeComponents
    ) {}

    public record UpdateCourseRequest(
            @NotBlank @Size(max = 150) String name,
            @NotNull @Min(1) @Max(15) Integer credits,
            @NotNull @Min(0) Integer theoryHours,
            @NotNull @Min(0) Integer practiceHours,
            @NotNull Long departmentId,
            @NotNull Faculty.Status status,
            @NotEmpty @Valid List<GradeComponentDto> gradeComponents
    ) {}

    public record PrerequisiteItemRequest(
            @NotNull Long prerequisiteCourseId,
            @NotNull CoursePrerequisite.ConditionType conditionType
    ) {}

    public record UpdatePrerequisitesRequest(
            @Valid List<PrerequisiteItemRequest> prerequisites
    ) {}

    public record CurriculumCourseItemDto(
            Long id,
            @NotNull Long courseId,
            String courseCode,
            String courseName,
            Integer credits,
            @NotNull @Min(1) @Max(10) Integer recommendedSemester,
            @NotNull Boolean isRequired
    ) {}

    public record CurriculumDto(
            Long id,
            String code,
            String name,
            Long majorId,
            String majorCode,
            String majorName,
            Integer cohortYear,
            Integer totalCredits,
            String status
    ) {}

    public record CurriculumDetailDto(
            Long id,
            String code,
            String name,
            Long majorId,
            String majorCode,
            String majorName,
            Integer cohortYear,
            Integer totalCredits,
            String status,
            List<CurriculumCourseItemDto> courses
    ) {}

    public record CreateCurriculumRequest(
            @NotBlank @Size(max = 30) String code,
            @NotBlank @Size(max = 150) String name,
            @NotNull Long majorId,
            @NotNull @Min(2000) @Max(2100) Integer cohortYear,
            @NotNull @Min(1) Integer totalCredits,
            @NotEmpty @Valid List<CurriculumCourseItemDto> courses
    ) {}
}
