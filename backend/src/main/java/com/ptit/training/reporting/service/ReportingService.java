package com.ptit.training.reporting.service;

import com.ptit.training.common.PageResponse;
import com.ptit.training.common.exception.DataScopeAccessDeniedException;
import com.ptit.training.common.security.UserPrincipal;
import com.ptit.training.enrollment.entity.Enrollment;
import com.ptit.training.enrollment.repository.EnrollmentRepository;
import com.ptit.training.reporting.dto.ReportingDtos.*;
import com.ptit.training.reporting.entity.AuditLog;
import com.ptit.training.reporting.repository.AuditLogRepository;
import com.ptit.training.section.entity.CourseSection;
import com.ptit.training.section.repository.CourseSectionRepository;
import com.ptit.training.student.entity.Student;
import com.ptit.training.student.repository.StudentRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Báo cáo thống kê đào tạo (FR-RPT-01) và tra cứu Audit Log (FR-SYS-02).
 */
@Service
public class ReportingService {

    private final AuditLogRepository auditLogRepo;
    private final StudentRepository studentRepo;
    private final CourseSectionRepository sectionRepo;
    private final EnrollmentRepository enrollmentRepo;

    public ReportingService(
            AuditLogRepository auditLogRepo,
            StudentRepository studentRepo,
            CourseSectionRepository sectionRepo,
            EnrollmentRepository enrollmentRepo) {
        this.auditLogRepo = auditLogRepo;
        this.studentRepo = studentRepo;
        this.sectionRepo = sectionRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> searchAuditLogs(Long actorUserId, String action, String entityType, Pageable pageable) {
        String cleanAction = (action == null || action.isBlank()) ? null : action.trim();
        String cleanEntity = (entityType == null || entityType.isBlank()) ? null : entityType.trim();
        Page<AuditLog> page = auditLogRepo.search(actorUserId, cleanAction, cleanEntity, pageable);
        return PageResponse.from(page.map(a -> new AuditLogDto(
                a.getId(),
                a.getActorUser().getId(),
                a.getActorUser().getUsername(),
                a.getAction(),
                a.getEntityType(),
                a.getEntityId(),
                a.getOldValueJson(),
                a.getNewValueJson(),
                a.getReason(),
                a.getCreatedAt()
        )));
    }

    @Transactional(readOnly = true)
    public StudentStatisticsReportDto getStudentStatistics(Long facultyId, Integer cohortYear) {
        List<Student> students = studentRepo.findAll().stream()
                .filter(s -> facultyId == null || s.getMajor().getFaculty().getId().equals(facultyId))
                .filter(s -> cohortYear == null || s.getCohortYear().equals(cohortYear))
                .toList();

        List<LabelCountDto> byStatus = students.stream()
                .collect(Collectors.groupingBy(s -> s.getAcademicStatus().name(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new LabelCountDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(LabelCountDto::label))
                .toList();

        List<LabelCountDto> byMajor = students.stream()
                .collect(Collectors.groupingBy(s -> s.getMajor().getName(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new LabelCountDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(LabelCountDto::label))
                .toList();

        List<LabelCountDto> byCohort = students.stream()
                .collect(Collectors.groupingBy(s -> "Khóa " + s.getCohortYear(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new LabelCountDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(LabelCountDto::label))
                .toList();

        return new StudentStatisticsReportDto(students.size(), byStatus, byMajor, byCohort);
    }

    @Transactional(readOnly = true)
    public SectionCapacityReportDto getEnrollmentCapacityReport(Long semesterId) {
        List<CourseSection> sections = sectionRepo.findAll().stream()
                .filter(s -> semesterId == null || s.getSemester().getId().equals(semesterId))
                .sorted(Comparator.comparing(CourseSection::getSectionCode))
                .toList();

        long totalEnrolled = 0;
        long totalMax = 0;
        List<SectionCapacityItemDto> items = new ArrayList<>();

        for (CourseSection cs : sections) {
            totalEnrolled += cs.getCurrentEnrolled();
            totalMax += cs.getMaxCapacity();
            double rate = cs.getMaxCapacity() == 0 ? 0.0
                    : Math.round((cs.getCurrentEnrolled() * 1000.0) / cs.getMaxCapacity()) / 10.0;
            items.add(new SectionCapacityItemDto(
                    cs.getId(),
                    cs.getSectionCode(),
                    cs.getCourse().getCode(),
                    cs.getCourse().getName(),
                    cs.getCurrentEnrolled(),
                    cs.getMaxCapacity(),
                    rate,
                    cs.getStatus().name()
            ));
        }

        return new SectionCapacityReportDto(sections.size(), totalEnrolled, totalMax, items);
    }

    @Transactional(readOnly = true)
    public GradeDistributionReportDto getGradeDistribution(
            UserPrincipal principal, Long semesterId, Long courseId, Long sectionId) {

        boolean isPrivileged = principal.hasRole("ADMIN") || principal.hasRole("ACADEMIC_AFFAIRS");
        List<Enrollment> enrollments = enrollmentRepo.findAll().stream()
                .filter(e -> e.getStatus() == Enrollment.Status.ENROLLED)
                .filter(e -> e.getLetterGrade() != null)
                .filter(e -> semesterId == null || e.getSemester().getId().equals(semesterId))
                .filter(e -> courseId == null || e.getCourse().getId().equals(courseId))
                .filter(e -> sectionId == null || e.getSection().getId().equals(sectionId))
                .toList();

        if (!isPrivileged) {
            enrollments = enrollments.stream()
                    .filter(e -> e.getSection().getLecturer() != null
                            && e.getSection().getLecturer().getUser().getId().equals(principal.getId()))
                    .toList();
            if (sectionId != null && enrollments.isEmpty()) {
                CourseSection sec = sectionRepo.findWithDetailsById(sectionId).orElse(null);
                if (sec != null && (sec.getLecturer() == null
                        || !sec.getLecturer().getUser().getId().equals(principal.getId()))) {
                    throw new DataScopeAccessDeniedException("ACCESS_DENIED_DATA_SCOPE",
                            "Giảng viên chỉ xem được phổ điểm của lớp mình giảng dạy");
                }
            }
        }

        long total = enrollments.size();
        long passed = enrollments.stream().filter(e -> Boolean.TRUE.equals(e.getIsPassed())).count();
        long failed = total - passed;
        double passRate = total == 0 ? 0.0 : Math.round((passed * 1000.0) / total) / 10.0;

        List<String> order = List.of("A", "B+", "B", "C+", "C", "D+", "D", "F");
        Map<String, Long> counts = enrollments.stream()
                .collect(Collectors.groupingBy(Enrollment::getLetterGrade, Collectors.counting()));
        List<LabelCountDto> dist = order.stream()
                .map(letter -> new LabelCountDto(letter, counts.getOrDefault(letter, 0L)))
                .toList();

        return new GradeDistributionReportDto(total, passed, failed, passRate, dist);
    }
}
