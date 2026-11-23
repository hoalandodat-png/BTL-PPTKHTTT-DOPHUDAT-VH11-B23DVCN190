package com.ptit.training.enrollment.service;

import com.ptit.training.auth.entity.User;
import com.ptit.training.auth.repository.UserRepository;
import com.ptit.training.common.exception.BusinessException;
import com.ptit.training.common.exception.ConflictException;
import com.ptit.training.common.exception.DataScopeAccessDeniedException;
import com.ptit.training.common.exception.ResourceNotFoundException;
import com.ptit.training.course.entity.Course;
import com.ptit.training.course.entity.CoursePrerequisite;
import com.ptit.training.course.entity.Curriculum;
import com.ptit.training.course.entity.CurriculumCourse;
import com.ptit.training.course.repository.CoursePrerequisiteRepository;
import com.ptit.training.course.repository.CurriculumRepository;
import com.ptit.training.enrollment.dto.EnrollmentDtos.*;
import com.ptit.training.enrollment.entity.Enrollment;
import com.ptit.training.enrollment.repository.EnrollmentRepository;
import com.ptit.training.organization.entity.Major;
import com.ptit.training.reporting.service.AuditLogService;
import com.ptit.training.section.entity.CourseSection;
import com.ptit.training.section.entity.SectionSchedule;
import com.ptit.training.section.entity.Semester;
import com.ptit.training.section.repository.CourseSectionRepository;
import com.ptit.training.section.repository.SectionScheduleRepository;
import com.ptit.training.section.repository.SemesterRepository;
import com.ptit.training.section.service.ScheduleConflictValidator;
import com.ptit.training.student.entity.Student;
import com.ptit.training.student.repository.StudentRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xử lý Đăng ký học phần, Hủy đăng ký, Đăng ký/Hủy hộ và Khóa dòng đồng thời (BR-19 -> BR-27).
 */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepo;
    private final StudentRepository studentRepo;
    private final CourseSectionRepository sectionRepo;
    private final SectionScheduleRepository scheduleRepo;
    private final SemesterRepository semesterRepo;
    private final CoursePrerequisiteRepository prereqRepo;
    private final CurriculumRepository curriculumRepo;
    private final UserRepository userRepo;
    private final ScheduleConflictValidator conflictValidator;
    private final AuditLogService auditLogService;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepo,
            StudentRepository studentRepo,
            CourseSectionRepository sectionRepo,
            SectionScheduleRepository scheduleRepo,
            SemesterRepository semesterRepo,
            CoursePrerequisiteRepository prereqRepo,
            CurriculumRepository curriculumRepo,
            UserRepository userRepo,
            ScheduleConflictValidator conflictValidator,
            AuditLogService auditLogService) {
        this.enrollmentRepo = enrollmentRepo;
        this.studentRepo = studentRepo;
        this.sectionRepo = sectionRepo;
        this.scheduleRepo = scheduleRepo;
        this.semesterRepo = semesterRepo;
        this.prereqRepo = prereqRepo;
        this.curriculumRepo = curriculumRepo;
        this.userRepo = userRepo;
        this.conflictValidator = conflictValidator;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public StudentSemesterEnrollmentDto getMyEnrollments(Long currentUserId, Long semesterId) {
        Student student = studentRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Không tìm thấy hồ sơ sinh viên"));
        Semester semester = semesterRepo.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("SEMESTER_NOT_FOUND", "Không tìm thấy học kỳ"));

        List<Enrollment> activeList = enrollmentRepo.findActiveByStudentAndSemesterWithDetails(student.getId(), semesterId);
        int totalCredits = activeList.stream().mapToInt(e -> e.getCourse().getCredits()).sum();
        int minAllowedCredits = resolveEffectiveMinCredits(semester, student);
        int maxAllowedCredits = resolveEffectiveMaxCredits(semester);

        return new StudentSemesterEnrollmentDto(
                semester.getId(),
                semester.getCode(),
                semester.getName(),
                minAllowedCredits,
                maxAllowedCredits,
                totalCredits,
                minAllowedCredits > 0 && totalCredits > 0 && totalCredits < minAllowedCredits,
                semester.isRegistrationActive(Instant.now()),
                semester.getTermType().name(),
                student.getMajor().getDegreeLevel().name(),
                activeList.stream().map(this::toDto).toList()
        );
    }

    /**
     * Sinh viên tự đăng ký lớp học phần trong Transaction với thứ tự khóa cố định: Student -> CourseSection.
     */
    @Transactional
    public EnrollmentResponseDto enrollStudent(Long currentUserId, Long sectionId) {
        Student student = studentRepo.findByUserIdForUpdate(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Không tìm thấy hồ sơ sinh viên"));

        if (!student.canEnroll()) {
            throw new DataScopeAccessDeniedException("ENROLL_STUDENT_NOT_ACTIVE",
                    "Trạng thái học vụ hiện tại (" + student.getAcademicStatus() + ") không được phép đăng ký học phần");
        }

        return executeEnroll(student, sectionId, currentUserId, false);
    }

    /**
     * Sinh viên tự hủy đăng ký trong thời gian mở đợt đăng ký (BR-20, BR-26).
     */
    @Transactional
    public void cancelStudentEnrollment(Long currentUserId, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepo.findWithDetailsById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("ENROLLMENT_NOT_FOUND", "Không tìm thấy bản ghi đăng ký"));

        if (!enrollment.getStudent().getUser().getId().equals(currentUserId)) {
            throw new DataScopeAccessDeniedException("ACCESS_DENIED_DATA_SCOPE",
                    "Bạn không có quyền hủy đăng ký học phần của sinh viên khác");
        }
        if (enrollment.getStatus() != Enrollment.Status.ENROLLED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ENROLLMENT_ALREADY_CANCELLED",
                    "Học phần này đã được hủy trước đó");
        }

        Semester semester = enrollment.getSemester();
        if (!semester.isRegistrationActive(Instant.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ENROLL_PERIOD_CLOSED",
                    "Đã hết thời gian tự hủy đăng ký. Vui lòng liên hệ Phòng đào tạo");
        }

        CourseSection section = sectionRepo.findByIdForUpdate(enrollment.getSection().getId())
                .orElseThrow(() -> new ResourceNotFoundException("SECTION_NOT_FOUND", "Không tìm thấy lớp học phần"));

        if (section.getGradeStatus() != CourseSection.GradeBookStatus.DRAFT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ENROLL_GRADE_PUBLISHED",
                    "Không thể hủy lớp học phần đã công bố hoặc khóa điểm");
        }

        enrollment.setStatus(Enrollment.Status.CANCELLED);
        enrollment.setCancelledAt(Instant.now());
        enrollment.setCancelledBy(userRepo.getReferenceById(currentUserId));
        enrollment.setCancelReason("Sinh viên tự hủy trong đợt đăng ký");
        enrollmentRepo.save(enrollment);

        section.decrementEnrolled();
        sectionRepo.save(section);
    }

    /**
     * Phòng đào tạo đăng ký hoặc hủy đăng ký hộ sinh viên (FR-ENR-03, BR-20, BR-32).
     */
    @Transactional
    public EnrollmentResponseDto overrideEnrollment(Long aaUserId, OverrideEnrollRequest req) {
        Student student = studentRepo.findByIdForUpdate(req.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Không tìm thấy sinh viên"));

        if (req.action() == OverrideAction.ENROLL) {
            EnrollmentResponseDto res = executeEnroll(student, req.sectionId(), aaUserId, true);
            auditLogService.record(aaUserId, "OVERRIDE_ENROLLMENT_ADD", "ENROLLMENT", String.valueOf(res.id()),
                    null, Map.of("studentCode", student.getStudentCode(), "sectionCode", res.sectionCode()),
                    req.reason().trim());
            return res;
        } else {
            CourseSection section = sectionRepo.findByIdForUpdate(req.sectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("SECTION_NOT_FOUND", "Không tìm thấy lớp học phần"));
            Enrollment enrollment = enrollmentRepo.findByStudentIdAndSectionId(student.getId(), section.getId())
                    .filter(e -> e.getStatus() == Enrollment.Status.ENROLLED)
                    .orElseThrow(() -> new ResourceNotFoundException("ENROLLMENT_NOT_FOUND",
                            "Sinh viên hiện không có trạng thái đang đăng ký tại lớp học phần này"));

            if (section.getGradeStatus() == CourseSection.GradeBookStatus.LOCKED) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "ENROLL_GRADE_LOCKED",
                        "Không thể hủy đăng ký khi bảng điểm lớp đã bị khóa sổ");
            }

            enrollment.setStatus(Enrollment.Status.CANCELLED);
            enrollment.setCancelledAt(Instant.now());
            enrollment.setCancelledBy(userRepo.getReferenceById(aaUserId));
            enrollment.setCancelReason(req.reason().trim());
            Enrollment saved = enrollmentRepo.save(enrollment);

            section.decrementEnrolled();
            sectionRepo.save(section);

            auditLogService.record(aaUserId, "OVERRIDE_ENROLLMENT_CANCEL", "ENROLLMENT", String.valueOf(saved.getId()),
                    Map.of("status", "ENROLLED"),
                    Map.of("status", "CANCELLED"),
                    req.reason().trim());

            return toDto(saved);
        }
    }

    @Transactional(readOnly = true)
    public CurriculumProgressDto getCurriculumProgress(Long currentUserId) {
        Student student = studentRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Không tìm thấy hồ sơ sinh viên"));
        Curriculum curriculum = curriculumRepo.findByMajorIdAndCohortYear(
                        student.getMajor().getId(), student.getCohortYear())
                .orElseThrow(() -> new ResourceNotFoundException("CURRICULUM_NOT_FOUND",
                        "Chưa có khung chương trình đào tạo cho ngành và khóa của bạn"));

        List<Enrollment> allEnrollments = enrollmentRepo.findAllEnrolledByStudentIdWithDetails(student.getId());
        Map<Long, List<Enrollment>> byCourse = new HashMap<>();
        for (Enrollment e : allEnrollments) {
            byCourse.computeIfAbsent(e.getCourse().getId(), k -> new ArrayList<>()).add(e);
        }

        int accumulatedCredits = 0;
        List<CurriculumProgressItemDto> items = new ArrayList<>();
        for (CurriculumCourse cc : curriculum.getCurriculumCourses()) {
            Course course = cc.getCourse();
            List<Enrollment> attempts = byCourse.getOrDefault(course.getId(), List.of());

            String status = "NOT_STARTED";
            String bestLetter = null;

            List<Enrollment> publishedAttempts = attempts.stream()
                    .filter(a -> a.getSection().getGradeStatus() != CourseSection.GradeBookStatus.DRAFT
                            && a.getFinalScore10() != null)
                    .toList();

            boolean hasPassed = publishedAttempts.stream().anyMatch(a -> Boolean.TRUE.equals(a.getIsPassed()));
            boolean hasActiveInDraft = attempts.stream()
                    .anyMatch(a -> a.getSection().getGradeStatus() == CourseSection.GradeBookStatus.DRAFT);

            if (hasPassed) {
                status = "PASSED";
                accumulatedCredits += course.getCredits();
                bestLetter = publishedAttempts.stream()
                        .max(Comparator.comparing(Enrollment::getGpaScore4)
                                .thenComparing(Enrollment::getFinalScore10))
                        .map(Enrollment::getLetterGrade)
                        .orElse(null);
            } else if (hasActiveInDraft) {
                status = "ENROLLED";
            } else if (!publishedAttempts.isEmpty()) {
                status = "FAILED";
                bestLetter = "F";
            }

            items.add(new CurriculumProgressItemDto(
                    course.getId(), course.getCode(), course.getName(), course.getCredits(),
                    cc.getRecommendedSemester(), cc.getIsRequired(), status, bestLetter));
        }

        return new CurriculumProgressDto(
                curriculum.getCode(), curriculum.getName(),
                curriculum.getTotalCredits(), accumulatedCredits, items);
    }

    private EnrollmentResponseDto executeEnroll(Student student, Long sectionId, Long actorUserId, boolean isOverride) {
        CourseSection section = sectionRepo.findByIdForUpdate(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("SECTION_NOT_FOUND", "Không tìm thấy lớp học phần"));

        Semester semester = section.getSemester();
        Course course = section.getCourse();

        // 1. Kiểm tra trạng thái lớp & đợt đăng ký (BR-20)
        if (section.getStatus() != CourseSection.Status.OPEN) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ENROLL_SECTION_CLOSED",
                    "Lớp học phần " + section.getSectionCode() + " hiện không mở đăng ký");
        }
        if (!isOverride && !semester.isRegistrationActive(Instant.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ENROLL_PERIOD_CLOSED",
                    "Ngoài thời gian mở đợt đăng ký của học kỳ " + semester.getCode());
        }

        // 2. Kiểm tra sĩ số (BR-25)
        if (!section.hasAvailableSeat()) {
            throw new ConflictException("ENROLL_SECTION_FULL",
                    "Lớp học phần " + section.getSectionCode() + " đã đủ sĩ số ("
                            + section.getCurrentEnrolled() + "/" + section.getMaxCapacity() + ")");
        }

        // 3. Kiểm tra chống đăng ký trùng học phần trong cùng học kỳ (BR-21)
        if (enrollmentRepo.existsByStudentIdAndCourseIdAndSemesterIdAndStatus(
                student.getId(), course.getId(), semester.getId(), Enrollment.Status.ENROLLED)) {
            throw new ConflictException("ENROLL_DUPLICATE_COURSE_IN_SEMESTER",
                    "Bạn đã đăng ký học phần " + course.getCode() + " trong học kỳ " + semester.getCode());
        }

        // 4. Kiểm tra điều kiện Tiên quyết & Học trước (BR-22)
        if (!isOverride) {
            validatePrerequisites(student.getId(), course, semester.getId());
        }

        // 5. Kiểm tra giới hạn tín chỉ tối đa theo loại học kỳ (Kỳ chính: 25 TC, Kỳ phụ hè: 12 TC - BR-23)
        int maxAllowedCredits = resolveEffectiveMaxCredits(semester);
        int currentCredits = enrollmentRepo.sumActiveCreditsByStudentAndSemester(student.getId(), semester.getId());
        if (currentCredits + course.getCredits() > maxAllowedCredits) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ENROLL_MAX_CREDITS_EXCEEDED",
                    String.format("Vượt quá giới hạn tín chỉ tối đa học kỳ (%d/%d tín chỉ)",
                            currentCredits + course.getCredits(), maxAllowedCredits));
        }

        // 6. Kiểm tra trùng lịch học với các lớp đã đăng ký trong kỳ (BR-24)
        List<SectionSchedule> candidateSchedules = scheduleRepo.findBySectionIdWithClassroom(section.getId());
        conflictValidator.validateStudentConflicts(student.getId(), semester.getId(), candidateSchedules);

        // 7. Ghi nhận đăng ký hoặc kích hoạt lại lượt đã hủy trước đó trong cùng lớp
        int previousAttempts = (int) enrollmentRepo.countPreviousAttempts(
                student.getId(), course.getId(), semester.getId());
        User actorUser = userRepo.getReferenceById(actorUserId);

        Optional<Enrollment> existingCancelled = enrollmentRepo.findByStudentIdAndSectionId(student.getId(), section.getId());
        Enrollment enrollment;
        if (existingCancelled.isPresent()) {
            enrollment = existingCancelled.get();
            enrollment.setStatus(Enrollment.Status.ENROLLED);
            enrollment.setAttemptNumber(previousAttempts + 1);
            enrollment.setEnrolledAt(Instant.now());
            enrollment.setEnrolledBy(actorUser);
            enrollment.setCancelledAt(null);
            enrollment.setCancelledBy(null);
            enrollment.setCancelReason(null);
        } else {
            enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setSection(section);
            enrollment.setCourse(course);
            enrollment.setSemester(semester);
            enrollment.setStatus(Enrollment.Status.ENROLLED);
            enrollment.setAttemptNumber(previousAttempts + 1);
            enrollment.setEnrolledBy(actorUser);
        }

        Enrollment saved = enrollmentRepo.save(enrollment);
        section.incrementEnrolled();
        sectionRepo.save(section);

        return toDto(saved);
    }

    private void validatePrerequisites(Long studentId, Course targetCourse, Long currentSemesterId) {
        List<CoursePrerequisite> prereqs = prereqRepo.findByCourseId(targetCourse.getId());
        if (prereqs.isEmpty()) return;

        List<Enrollment> history = enrollmentRepo.findAllEnrolledByStudentIdWithDetails(studentId);
        for (CoursePrerequisite cp : prereqs) {
            Course required = cp.getPrerequisiteCourse();
            List<Enrollment> priorAttempts = history.stream()
                    .filter(e -> e.getCourse().getId().equals(required.getId())
                            && !e.getSemester().getId().equals(currentSemesterId)
                            && e.getSection().getGradeStatus() != CourseSection.GradeBookStatus.DRAFT
                            && e.getFinalScore10() != null)
                    .toList();

            if (cp.getConditionType() == CoursePrerequisite.ConditionType.PREREQUISITE) {
                boolean passed = priorAttempts.stream().anyMatch(e -> Boolean.TRUE.equals(e.getIsPassed()));
                if (!passed) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "ENROLL_PREREQUISITE_NOT_MET",
                            "Chưa đạt học phần tiên quyết: " + required.getCode() + " - " + required.getName());
                }
            } else {
                if (priorAttempts.isEmpty()) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "ENROLL_PREREQUISITE_NOT_MET",
                            "Chưa học qua học phần học trước: " + required.getCode() + " - " + required.getName());
                }
            }
        }
    }

    /**
     * Xác định số tín chỉ tối thiểu theo loại học kỳ & hệ đào tạo:
     * - Học kỳ chính hệ cử nhân (BACHELOR): 15 TC
     * - Học kỳ chính hệ kỹ sư (ENGINEER): 16 TC
     * - Học kỳ phụ (hè - SUMMER): Không quy định chung (0 TC)
     */
    private int resolveEffectiveMinCredits(Semester semester, Student student) {
        if (semester.getTermType() == Semester.TermType.SUMMER) {
            return 0;
        }
        return student.getMajor().getDegreeLevel() == Major.DegreeLevel.ENGINEER ? 16 : 15;
    }

    /**
     * Xác định số tín chỉ tối đa theo loại học kỳ:
     * - Học kỳ chính (REGULAR): tối đa 25 TC
     * - Học kỳ phụ (hè - SUMMER): tối đa 12 TC
     */
    private int resolveEffectiveMaxCredits(Semester semester) {
        int cap = semester.getTermType() == Semester.TermType.SUMMER ? 12 : 25;
        return Math.min(semester.getMaxCredits(), cap);
    }

    private EnrollmentResponseDto toDto(Enrollment e) {
        return new EnrollmentResponseDto(
                e.getId(),
                e.getStudent().getId(),
                e.getStudent().getStudentCode(),
                e.getSection().getId(),
                e.getSection().getSectionCode(),
                e.getCourse().getId(),
                e.getCourse().getCode(),
                e.getCourse().getName(),
                e.getCourse().getCredits(),
                e.getSemester().getId(),
                e.getSemester().getCode(),
                e.getStatus().name(),
                e.getAttemptNumber(),
                e.getEnrolledAt(),
                e.getSection().getCurrentEnrolled(),
                e.getSection().getMaxCapacity()
        );
    }
}
