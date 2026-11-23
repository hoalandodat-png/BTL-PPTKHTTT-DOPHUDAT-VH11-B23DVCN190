package com.ptit.training.section.dto;

import com.ptit.training.section.entity.Classroom;
import com.ptit.training.section.entity.CourseSection;
import com.ptit.training.section.entity.Semester;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class SectionDtos {

    private SectionDtos() {}

    public record AcademicYearDto(Long id, String code, String name, Integer startYear, Integer endYear, String status) {}

    public record CreateAcademicYearRequest(
            @NotBlank @Size(max = 20) String code,
            @NotBlank @Size(max = 100) String name,
            @NotNull @Min(2000) @Max(2100) Integer startYear
    ) {}

    public record SemesterDto(
            Long id, String code, String name, Long academicYearId, String academicYearCode,
            String termType, LocalDate startDate, LocalDate endDate,
            Integer minCredits, Integer maxCredits,
            Instant registrationStartAt, Instant registrationEndAt,
            Boolean isRegistrationOpen, String status
    ) {}

    public record CreateSemesterRequest(
            @NotBlank @Size(max = 20) String code,
            @NotBlank @Size(max = 100) String name,
            @NotNull Long academicYearId,
            @NotNull Semester.TermType termType,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            @NotNull @Min(0) Integer minCredits,
            @NotNull @Min(1) @Max(25) Integer maxCredits,
            @NotNull Instant registrationStartAt,
            @NotNull Instant registrationEndAt
    ) {}

    public record UpdateRegistrationPeriodRequest(
            @NotNull Instant registrationStartAt,
            @NotNull Instant registrationEndAt,
            @NotNull Boolean isRegistrationOpen,
            @Min(0) Integer minCredits,
            @Min(1) @Max(25) Integer maxCredits,
            Semester.Status status
    ) {}

    public record ClassroomDto(Long id, String code, String building, Integer capacity, String roomType, String status) {}

    public record CreateClassroomRequest(
            @NotBlank @Size(max = 30) String code,
            @NotBlank @Size(max = 50) String building,
            @NotNull @Min(1) Integer capacity,
            @NotNull Classroom.RoomType roomType
    ) {}

    public record ScheduleItemRequest(
            @NotNull @Min(2) @Max(7) Integer dayOfWeek,
            @NotNull @Min(1) @Max(12) Integer startPeriod,
            @NotNull @Min(1) @Max(12) Integer endPeriod,
            @NotNull @Min(1) Integer startWeek,
            @NotNull @Min(1) @Max(25) Integer endWeek,
            @NotNull Long classroomId
    ) {}

    public record ScheduleItemDto(
            Long id, Integer dayOfWeek, Integer startPeriod, Integer endPeriod,
            Integer startWeek, Integer endWeek, Long classroomId, String classroomCode, String building
    ) {}

    public record CourseSectionDto(
            Long id,
            String sectionCode,
            Long courseId,
            String courseCode,
            String courseName,
            Integer credits,
            Long semesterId,
            String semesterCode,
            Integer groupNumber,
            Long lecturerId,
            String lecturerName,
            Integer maxCapacity,
            Integer currentEnrolled,
            String gradeStatus,
            String status,
            List<ScheduleItemDto> schedules
    ) {}

    public record CreateCourseSectionRequest(
            @NotNull Long courseId,
            @NotNull Long semesterId,
            @NotNull @Min(1) Integer groupNumber,
            Long lecturerId,
            @NotNull @Min(1) Integer maxCapacity,
            @NotEmpty @Valid List<ScheduleItemRequest> schedules
    ) {}

    public record UpdateCourseSectionRequest(
            Long lecturerId,
            @NotNull @Min(1) Integer maxCapacity,
            @NotNull CourseSection.Status status,
            @NotEmpty @Valid List<ScheduleItemRequest> schedules
    ) {}

    public record SectionStudentRosterDto(
            Long enrollmentId,
            Long studentId,
            String studentCode,
            String fullName,
            String academicClassCode,
            String email,
            Integer attemptNumber,
            Instant enrolledAt
    ) {}

    public record TimetableSlotDto(
            Long sectionId,
            String sectionCode,
            String courseCode,
            String courseName,
            Integer credits,
            String lecturerName,
            Integer dayOfWeek,
            Integer startPeriod,
            Integer endPeriod,
            Integer startWeek,
            Integer endWeek,
            String classroomCode,
            String building
    ) {}

    public record PersonalTimetableDto(
            Long semesterId,
            String semesterCode,
            String semesterName,
            String role,
            List<TimetableSlotDto> slots
    ) {}
}
