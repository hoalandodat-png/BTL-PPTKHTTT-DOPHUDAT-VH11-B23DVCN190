package com.ptit.training.organization.dto;

import com.ptit.training.organization.entity.Faculty;
import com.ptit.training.organization.entity.Major;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class OrganizationDtos {

    private OrganizationDtos() {}

    public record FacultyDto(Long id, String code, String name, String description, String status) {}

    public record CreateFacultyRequest(
            @NotBlank @Size(max = 20) String code,
            @NotBlank @Size(max = 150) String name,
            String description
    ) {}

    public record UpdateFacultyRequest(
            @NotBlank @Size(max = 150) String name,
            String description,
            @NotNull Faculty.Status status
    ) {}

    public record DepartmentDto(Long id, String code, String name, Long facultyId, String facultyName, String status) {}

    public record CreateDepartmentRequest(
            @NotBlank @Size(max = 20) String code,
            @NotBlank @Size(max = 150) String name,
            @NotNull Long facultyId
    ) {}

    public record UpdateDepartmentRequest(
            @NotBlank @Size(max = 150) String name,
            @NotNull Long facultyId,
            @NotNull Faculty.Status status
    ) {}

    public record MajorDto(Long id, String code, String name, Long facultyId, String facultyName,
                           Integer totalCreditsRequired, String degreeLevel, String status) {}

    public record CreateMajorRequest(
            @NotBlank @Size(max = 20) String code,
            @NotBlank @Size(max = 150) String name,
            @NotNull Long facultyId,
            @NotNull @Min(1) Integer totalCreditsRequired,
            Major.DegreeLevel degreeLevel
    ) {}

    public record UpdateMajorRequest(
            @NotBlank @Size(max = 150) String name,
            @NotNull Long facultyId,
            @NotNull @Min(1) Integer totalCreditsRequired,
            Major.DegreeLevel degreeLevel,
            @NotNull Faculty.Status status
    ) {}

    public record AcademicClassDto(Long id, String code, String name, Long majorId, String majorCode,
                                   String majorName, Integer cohortYear, String status) {}

    public record CreateAcademicClassRequest(
            @NotBlank @Size(max = 30) String code,
            @NotBlank @Size(max = 150) String name,
            @NotNull Long majorId,
            @NotNull @Min(2000) @Max(2100) Integer cohortYear
    ) {}

    public record UpdateAcademicClassRequest(
            @NotBlank @Size(max = 150) String name,
            @NotNull Long majorId,
            @NotNull @Min(2000) @Max(2100) Integer cohortYear,
            @NotNull Faculty.Status status
    ) {}

    public record LecturerDto(Long id, Long userId, String lecturerCode, String fullName, String email,
                              String degree, String academicRank, Long departmentId, String departmentName,
                              Long facultyId, String facultyName, String phone, String status) {}

    public record CreateLecturerRequest(
            @NotBlank @Size(max = 30) String lecturerCode,
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 120) String email,
            @NotNull Long departmentId,
            @Size(max = 50) String degree,
            @Size(max = 50) String academicRank,
            @Size(max = 20) String phone,
            String initialPassword
    ) {}

    public record UpdateLecturerRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotNull Long departmentId,
            @Size(max = 50) String degree,
            @Size(max = 50) String academicRank,
            @Size(max = 20) String phone,
            @NotNull Faculty.Status status
    ) {}
}
