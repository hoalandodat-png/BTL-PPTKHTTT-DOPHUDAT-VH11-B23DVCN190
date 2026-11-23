package com.ptit.training.section.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public final class ExamDtos {

    private ExamDtos() {}

    public record ExamScheduleDto(
            Long id,
            Long sectionId,
            String sectionCode,
            String courseCode,
            String courseName,
            Integer credits,
            Long semesterId,
            String semesterCode,
            Long classroomId,
            String classroomCode,
            String building,
            LocalDate examDate,
            LocalTime startTime,
            LocalTime endTime,
            String examFormat,
            String note
    ) {}

    public record CreateExamScheduleRequest(
            @NotNull Long sectionId,
            @NotNull Long classroomId,
            @NotNull LocalDate examDate,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime,
            @NotBlank @Size(max = 60) String examFormat,
            @Size(max = 255) String note
    ) {}
}
