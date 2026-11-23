package com.ptit.training.student.dto;

import com.ptit.training.student.entity.Student;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class StudentDtos {

    private StudentDtos() {}

    public record StudentSummaryDto(
            Long id,
            Long userId,
            String studentCode,
            String fullName,
            LocalDate dateOfBirth,
            String gender,
            String email,
            String phone,
            Long academicClassId,
            String academicClassCode,
            Long majorId,
            String majorCode,
            String majorName,
            Integer cohortYear,
            String academicStatus
    ) {}

    public record StatusHistoryDto(
            Long id,
            String oldStatus,
            String newStatus,
            String reason,
            String changedByUsername,
            Instant changedAt
    ) {}

    public record StudentDetailDto(
            Long id,
            Long userId,
            String studentCode,
            String fullName,
            LocalDate dateOfBirth,
            String gender,
            String nationalId,
            String email,
            String personalEmail,
            String phone,
            String address,
            String avatarUrl,
            Long academicClassId,
            String academicClassCode,
            String academicClassName,
            Long majorId,
            String majorCode,
            String majorName,
            Long facultyId,
            String facultyName,
            Integer cohortYear,
            String academicStatus,
            Instant createdAt,
            List<StatusHistoryDto> statusHistory
    ) {}

    public record CreateStudentRequest(
            @NotBlank @Size(max = 30) String studentCode,
            @NotBlank @Size(max = 120) String fullName,
            @NotNull @Past LocalDate dateOfBirth,
            @NotNull Student.Gender gender,
            @NotBlank @Size(max = 20) String nationalId,
            @NotBlank @Email @Size(max = 120) String email,
            @Email @Size(max = 120) String personalEmail,
            @Size(max = 20) String phone,
            @Size(max = 255) String address,
            @NotNull Long academicClassId,
            @NotNull Long majorId,
            @NotNull Integer cohortYear,
            String initialPassword
    ) {}

    public record UpdateStudentRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotNull @Past LocalDate dateOfBirth,
            @NotNull Student.Gender gender,
            @NotBlank @Size(max = 20) String nationalId,
            @Email @Size(max = 120) String personalEmail,
            @Size(max = 20) String phone,
            @Size(max = 255) String address,
            @NotNull Long academicClassId,
            @NotNull Long majorId,
            @NotNull Integer cohortYear
    ) {}

    public record UpdateAcademicStatusRequest(
            @NotNull Student.AcademicStatus newStatus,
            @NotBlank(message = "Bắt buộc nhập lý do chuyển trạng thái học vụ") String reason
    ) {}

    public record UpdateSelfContactRequest(
            @Size(max = 20) String phone,
            @Email @Size(max = 120) String personalEmail,
            @Size(max = 255) String address,
            @Size(max = 255) String avatarUrl
    ) {}

    public record ImportErrorRow(int rowNumber, String studentCode, String message) {}

    public record StudentImportResultDto(
            boolean dryRun,
            int totalRows,
            int validRows,
            int importedCount,
            List<ImportErrorRow> errors
    ) {}
}
