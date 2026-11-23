package com.ptit.training.reporting.dto;

import java.time.Instant;
import java.util.List;

public final class ReportingDtos {

    private ReportingDtos() {}

    public record AuditLogDto(
            Long id,
            Long actorUserId,
            String actorUsername,
            String action,
            String entityType,
            String entityId,
            String oldValueJson,
            String newValueJson,
            String reason,
            Instant createdAt
    ) {}

    public record LabelCountDto(String label, long count) {}

    public record StudentStatisticsReportDto(
            long totalStudents,
            List<LabelCountDto> byAcademicStatus,
            List<LabelCountDto> byMajor,
            List<LabelCountDto> byCohortYear
    ) {}

    public record SectionCapacityItemDto(
            Long sectionId,
            String sectionCode,
            String courseCode,
            String courseName,
            Integer currentEnrolled,
            Integer maxCapacity,
            double fillRatePercent,
            String status
    ) {}

    public record SectionCapacityReportDto(
            long totalSections,
            long totalEnrolledSeats,
            long totalMaxCapacity,
            List<SectionCapacityItemDto> sections
    ) {}

    public record GradeDistributionReportDto(
            long totalGraded,
            long passedCount,
            long failedCount,
            double passRatePercent,
            List<LabelCountDto> distribution
    ) {}
}
