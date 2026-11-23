package com.ptit.training.section.service;

import com.ptit.training.common.exception.ConflictException;
import com.ptit.training.enrollment.entity.Enrollment;
import com.ptit.training.enrollment.repository.EnrollmentRepository;
import com.ptit.training.section.entity.SectionSchedule;
import com.ptit.training.section.repository.SectionScheduleRepository;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Kiểm tra trùng lịch học theo 3 chiều (Phòng học, Giảng viên, Sinh viên) dựa trên BR-15.
 */
@Component
public class ScheduleConflictValidator {

    private final SectionScheduleRepository scheduleRepo;
    private final EnrollmentRepository enrollmentRepo;

    public ScheduleConflictValidator(SectionScheduleRepository scheduleRepo, EnrollmentRepository enrollmentRepo) {
        this.scheduleRepo = scheduleRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    public void validateInternalOverlap(List<SectionSchedule> schedules) {
        for (int i = 0; i < schedules.size(); i++) {
            for (int j = i + 1; j < schedules.size(); j++) {
                if (schedules.get(i).overlapsWith(schedules.get(j))) {
                    throw new ConflictException("SCHEDULE_SELF_OVERLAP",
                            "Các buổi học trong cùng một lớp học phần đang bị trùng giờ với nhau");
                }
            }
        }
    }

    public void validateClassroomConflicts(Long semesterId, Long excludeSectionId, List<SectionSchedule> candidates) {
        for (SectionSchedule candidate : candidates) {
            List<SectionSchedule> existing = scheduleRepo.findBySemesterAndClassroomExcludingSection(
                    semesterId, candidate.getClassroom().getId(), excludeSectionId);
            for (SectionSchedule other : existing) {
                if (candidate.overlapsWith(other)) {
                    throw new ConflictException("SCHEDULE_CLASSROOM_CONFLICT",
                            String.format("Trùng lịch phòng học %s (Thứ %d, Tiết %d-%d, Tuần %d-%d) với lớp %s",
                                    candidate.getClassroom().getCode(),
                                    candidate.getDayOfWeek(),
                                    other.getStartPeriod(), other.getEndPeriod(),
                                    other.getStartWeek(), other.getEndWeek(),
                                    other.getSection().getSectionCode()));
                }
            }
        }
    }

    public void validateLecturerConflicts(Long semesterId, Long lecturerId, Long excludeSectionId, List<SectionSchedule> candidates) {
        if (lecturerId == null) return;
        List<SectionSchedule> existing = scheduleRepo.findBySemesterAndLecturerExcludingSection(
                semesterId, lecturerId, excludeSectionId);
        for (SectionSchedule candidate : candidates) {
            for (SectionSchedule other : existing) {
                if (candidate.overlapsWith(other)) {
                    throw new ConflictException("SCHEDULE_LECTURER_CONFLICT",
                            String.format("Trùng lịch giảng viên (Thứ %d, Tiết %d-%d, Tuần %d-%d) với lớp %s",
                                    candidate.getDayOfWeek(),
                                    other.getStartPeriod(), other.getEndPeriod(),
                                    other.getStartWeek(), other.getEndWeek(),
                                    other.getSection().getSectionCode()));
                }
            }
        }
    }

    public void validateStudentConflicts(Long studentId, Long semesterId, List<SectionSchedule> candidateSchedules) {
        List<Enrollment> currentEnrollments = enrollmentRepo.findActiveByStudentAndSemesterWithDetails(studentId, semesterId);
        for (Enrollment e : currentEnrollments) {
            List<SectionSchedule> existingSchedules = scheduleRepo.findBySectionIdWithClassroom(e.getSection().getId());
            for (SectionSchedule candidate : candidateSchedules) {
                for (SectionSchedule existing : existingSchedules) {
                    if (candidate.overlapsWith(existing)) {
                        throw new ConflictException("ENROLL_SCHEDULE_CONFLICT",
                                String.format("Trùng lịch học vào Thứ %d (Tiết %d-%d, Tuần %d-%d) với lớp %s",
                                        candidate.getDayOfWeek(),
                                        existing.getStartPeriod(), existing.getEndPeriod(),
                                        existing.getStartWeek(), existing.getEndWeek(),
                                        e.getSection().getSectionCode()));
                    }
                }
            }
        }
    }
}
