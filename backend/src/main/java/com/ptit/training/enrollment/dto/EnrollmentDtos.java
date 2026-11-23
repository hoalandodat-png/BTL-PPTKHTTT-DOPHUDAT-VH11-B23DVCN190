package com.ptit.training.enrollment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public final class EnrollmentDtos {

    private EnrollmentDtos() {}

    public enum OverrideAction { ENROLL, CANCEL }

    public record EnrollRequest(
            @NotNull(message = "Vui lòng chọn lớp học phần") Long sectionId
    ) {}

    public record OverrideEnrollRequest(
            @NotNull Long studentId,
            @NotNull Long sectionId,
            @NotNull OverrideAction action,
            @NotBlank(message = "Bắt buộc nhập lý do thao tác hộ") String reason
    ) {}

    public record EnrollmentResponseDto(
            Long id,
            Long studentId,
            String studentCode,
            Long sectionId,
            String sectionCode,
            Long courseId,
            String courseCode,
            String courseName,
            Integer credits,
            Long semesterId,
            String semesterCode,
            String status,
            Integer attemptNumber,
            Instant enrolledAt,
            Integer currentEnrolled,
            Integer maxCapacity
    ) {}

    public record StudentSemesterEnrollmentDto(
            Long semesterId,
            String semesterCode,
            String semesterName,
            Integer minCredits,
            Integer maxCredits,
            Integer totalEnrolledCredits,
            Boolean belowMinCreditsWarning,
            Boolean isRegistrationOpen,
            String termType,
            String degreeLevel,
            List<EnrollmentResponseDto> items
    ) {}

    public record CurriculumProgressItemDto(
            Long courseId,
            String courseCode,
            String courseName,
            Integer credits,
            Integer recommendedSemester,
            Boolean isRequired,
            String completionStatus, // PASSED, ENROLLED, FAILED, NOT_STARTED
            String bestLetterGrade
    ) {}

    public record CurriculumProgressDto(
            String curriculumCode,
            String curriculumName,
            Integer totalCreditsRequired,
            Integer accumulatedCredits,
            List<CurriculumProgressItemDto> items
    ) {}
}
