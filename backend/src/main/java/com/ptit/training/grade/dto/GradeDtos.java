package com.ptit.training.grade.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class GradeDtos {

    private GradeDtos() {}

    public record ComponentHeaderDto(
            Long id, String code, String name, Integer weightPercent, Boolean isFinalExam, BigDecimal minThreshold
    ) {}

    public record ComponentScoreInput(
            @NotNull Long gradeComponentId,
            @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal score
    ) {}

    public record StudentGradeRowInput(
            @NotNull Long enrollmentId,
            @NotEmpty @Valid List<ComponentScoreInput> scores
    ) {}

    public record SaveSectionGradesRequest(
            @NotEmpty @Valid List<StudentGradeRowInput> rows
    ) {}

    public record OverrideScoreRequest(
            @NotEmpty @Valid List<ComponentScoreInput> scores,
            @NotBlank(message = "Bắt buộc nhập lý do sửa điểm sau công bố/khóa") String reason
    ) {}

    public record StudentGradeRowDto(
            Long enrollmentId,
            Long studentId,
            String studentCode,
            String fullName,
            String academicClassCode,
            Map<Long, BigDecimal> componentScores,
            BigDecimal finalScore10,
            String letterGrade,
            BigDecimal gpaScore4,
            Boolean isPassed
    ) {}

    public record SectionGradeBookDto(
            Long sectionId,
            String sectionCode,
            String courseCode,
            String courseName,
            String gradeStatus,
            Instant publishedAt,
            Instant lockedAt,
            List<ComponentHeaderDto> components,
            List<StudentGradeRowDto> rows
    ) {}

    public record CourseTranscriptItemDto(
            Long enrollmentId,
            String sectionCode,
            String courseCode,
            String courseName,
            Integer credits,
            Integer attemptNumber,
            String gradeStatus,
            Map<String, BigDecimal> componentScoresByName,
            BigDecimal finalScore10,
            String letterGrade,
            BigDecimal gpaScore4,
            Boolean isPassed
    ) {}

    public record SemesterTranscriptDto(
            Long semesterId,
            String semesterCode,
            String semesterName,
            Integer registeredCredits,
            BigDecimal semesterGpa4,
            List<CourseTranscriptItemDto> courses
    ) {}

    public record AcademicTranscriptDto(
            Long studentId,
            String studentCode,
            String fullName,
            String majorName,
            String academicClassCode,
            BigDecimal cumulativeCpa4,
            Integer totalAccumulatedCredits,
            List<SemesterTranscriptDto> semesters
    ) {}

    public record GradingScaleDto(
            Long id,
            @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal minScore10,
            @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal maxScore10,
            @NotBlank String letterGrade,
            @NotNull @DecimalMin("0.0") @DecimalMax("4.0") BigDecimal gpaScore4,
            @NotNull Boolean isPass,
            @NotNull Integer displayOrder
    ) {}

    public record UpdateGradingScalesRequest(
            @NotEmpty @Valid List<GradingScaleDto> scales
    ) {}

    public record SystemConfigDto(
            String configKey,
            String configValue,
            String valueType,
            String description
    ) {}

    public record ImportGradeErrorRow(int rowNumber, String studentCode, String message) {}

    public record ImportGradeResultDto(
            boolean dryRun,
            int totalRows,
            int validRows,
            int updatedCount,
            List<ImportGradeErrorRow> errors
    ) {}
}
