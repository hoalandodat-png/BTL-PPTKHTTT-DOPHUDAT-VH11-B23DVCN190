package com.ptit.training.section.service;

import com.ptit.training.common.PageResponse;
import com.ptit.training.common.exception.BusinessException;
import com.ptit.training.common.exception.ConflictException;
import com.ptit.training.common.exception.DataScopeAccessDeniedException;
import com.ptit.training.common.exception.ResourceNotFoundException;
import com.ptit.training.common.security.UserPrincipal;
import com.ptit.training.course.entity.Course;
import com.ptit.training.course.repository.CourseRepository;
import com.ptit.training.enrollment.entity.Enrollment;
import com.ptit.training.enrollment.repository.EnrollmentRepository;
import com.ptit.training.organization.entity.Lecturer;
import com.ptit.training.organization.repository.LecturerRepository;
import com.ptit.training.reporting.service.AuditLogService;
import com.ptit.training.section.dto.SectionDtos.*;
import com.ptit.training.section.entity.*;
import com.ptit.training.section.repository.*;
import com.ptit.training.student.entity.Student;
import com.ptit.training.student.repository.StudentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quản lý Năm học, Học kỳ, Đợt đăng ký, Phòng học, Lớp học phần và Thời khóa biểu.
 */
@Service
public class SectionService {

    private final AcademicYearRepository yearRepo;
    private final SemesterRepository semesterRepo;
    private final ClassroomRepository classroomRepo;
    private final CourseSectionRepository sectionRepo;
    private final SectionScheduleRepository scheduleRepo;
    private final CourseRepository courseRepo;
    private final LecturerRepository lecturerRepo;
    private final StudentRepository studentRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final ScheduleConflictValidator conflictValidator;
    private final AuditLogService auditLogService;

    public SectionService(
            AcademicYearRepository yearRepo,
            SemesterRepository semesterRepo,
            ClassroomRepository classroomRepo,
            CourseSectionRepository sectionRepo,
            SectionScheduleRepository scheduleRepo,
            CourseRepository courseRepo,
            LecturerRepository lecturerRepo,
            StudentRepository studentRepo,
            EnrollmentRepository enrollmentRepo,
            ScheduleConflictValidator conflictValidator,
            AuditLogService auditLogService) {
        this.yearRepo = yearRepo;
        this.semesterRepo = semesterRepo;
        this.classroomRepo = classroomRepo;
        this.sectionRepo = sectionRepo;
        this.scheduleRepo = scheduleRepo;
        this.courseRepo = courseRepo;
        this.lecturerRepo = lecturerRepo;
        this.studentRepo = studentRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.conflictValidator = conflictValidator;
        this.auditLogService = auditLogService;
    }

    // --- ACADEMIC YEARS ---
    @Transactional(readOnly = true)
    public List<AcademicYearDto> listAcademicYears() {
        return yearRepo.findAllByOrderByStartYearDesc().stream()
                .map(y -> new AcademicYearDto(y.getId(), y.getCode(), y.getName(),
                        y.getStartYear(), y.getEndYear(), y.getStatus().name()))
                .toList();
    }

    @Transactional
    public AcademicYearDto createAcademicYear(CreateAcademicYearRequest req) {
        String code = req.code().trim();
        if (yearRepo.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("ACADEMIC_YEAR_EXISTS", "Mã năm học đã tồn tại: " + code);
        }
        AcademicYear y = new AcademicYear();
        y.setCode(code);
        y.setName(req.name().trim());
        y.setStartYear(req.startYear());
        y.setEndYear(req.startYear() + 1);
        AcademicYear saved = yearRepo.save(y);
        return new AcademicYearDto(saved.getId(), saved.getCode(), saved.getName(),
                saved.getStartYear(), saved.getEndYear(), saved.getStatus().name());
    }

    // --- SEMESTERS ---
    @Transactional(readOnly = true)
    public List<SemesterDto> listSemesters() {
        return semesterRepo.findAllByOrderByStartDateDesc().stream().map(this::toSemesterDto).toList();
    }

    @Transactional
    public SemesterDto createSemester(CreateSemesterRequest req) {
        String code = req.code().trim().toUpperCase();
        if (semesterRepo.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("SEMESTER_CODE_EXISTS", "Mã học kỳ đã tồn tại: " + code);
        }
        if (!req.endDate().isAfter(req.startDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE",
                    "Ngày kết thúc học kỳ phải sau ngày bắt đầu");
        }
        if (req.maxCredits() < req.minCredits()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_CREDIT_RANGE",
                    "Số tín chỉ tối đa không được nhỏ hơn số tín chỉ tối thiểu");
        }
        int cap = req.termType() == Semester.TermType.SUMMER ? 12 : 25;
        if (req.maxCredits() > cap) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "MAX_CREDITS_CAP_EXCEEDED",
                    req.termType() == Semester.TermType.SUMMER
                            ? "Học kỳ phụ (hè) tối đa không quá 12 tín chỉ"
                            : "Học kỳ chính tối đa không quá 25 tín chỉ");
        }
        if (!req.registrationEndAt().isAfter(req.registrationStartAt())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_REGISTRATION_WINDOW",
                    "Thời gian kết thúc đăng ký phải sau thời gian bắt đầu");
        }
        AcademicYear year = yearRepo.findById(req.academicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("ACADEMIC_YEAR_NOT_FOUND", "Không tìm thấy năm học"));

        Semester sem = new Semester();
        sem.setCode(code);
        sem.setName(req.name().trim());
        sem.setAcademicYear(year);
        sem.setTermType(req.termType());
        sem.setStartDate(req.startDate());
        sem.setEndDate(req.endDate());
        sem.setMinCredits(req.minCredits());
        sem.setMaxCredits(req.maxCredits());
        sem.setRegistrationStartAt(req.registrationStartAt());
        sem.setRegistrationEndAt(req.registrationEndAt());
        sem.setIsRegistrationOpen(false);
        sem.setStatus(Semester.Status.UPCOMING);
        return toSemesterDto(semesterRepo.save(sem));
    }

    @Transactional
    public SemesterDto updateRegistrationPeriod(Long actorUserId, Long semesterId, UpdateRegistrationPeriodRequest req) {
        Semester sem = semesterRepo.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("SEMESTER_NOT_FOUND", "Không tìm thấy học kỳ"));
        if (!req.registrationEndAt().isAfter(req.registrationStartAt())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_REGISTRATION_WINDOW",
                    "Thời gian kết thúc đăng ký phải sau thời gian bắt đầu");
        }
        if (req.minCredits() != null) sem.setMinCredits(req.minCredits());
        if (req.maxCredits() != null) {
            if (req.maxCredits() < sem.getMinCredits()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_CREDIT_RANGE",
                        "Số tín chỉ tối đa không được nhỏ hơn số tín chỉ tối thiểu");
            }
            int cap = sem.getTermType() == Semester.TermType.SUMMER ? 12 : 25;
            if (req.maxCredits() > cap) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "MAX_CREDITS_CAP_EXCEEDED",
                        sem.getTermType() == Semester.TermType.SUMMER
                                ? "Học kỳ phụ (hè) tối đa không quá 12 tín chỉ"
                                : "Học kỳ chính tối đa không quá 25 tín chỉ");
            }
            sem.setMaxCredits(req.maxCredits());
        }
        if (req.status() != null) sem.setStatus(req.status());

        boolean oldOpen = sem.getIsRegistrationOpen();
        sem.setRegistrationStartAt(req.registrationStartAt());
        sem.setRegistrationEndAt(req.registrationEndAt());
        sem.setIsRegistrationOpen(req.isRegistrationOpen());
        Semester saved = semesterRepo.save(sem);

        auditLogService.record(actorUserId, "UPDATE_REGISTRATION_PERIOD", "SEMESTER", String.valueOf(semesterId),
                Map.of("isRegistrationOpen", oldOpen),
                Map.of("isRegistrationOpen", req.isRegistrationOpen()),
                "Cập nhật cấu hình đợt đăng ký học kỳ " + sem.getCode());

        return toSemesterDto(saved);
    }

    // --- CLASSROOMS ---
    @Transactional(readOnly = true)
    public List<ClassroomDto> listClassrooms() {
        return classroomRepo.findAllByOrderByCodeAsc().stream()
                .map(c -> new ClassroomDto(c.getId(), c.getCode(), c.getBuilding(),
                        c.getCapacity(), c.getRoomType().name(), c.getStatus().name()))
                .toList();
    }

    @Transactional
    public ClassroomDto createClassroom(CreateClassroomRequest req) {
        String code = req.code().trim().toUpperCase();
        if (classroomRepo.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("CLASSROOM_CODE_EXISTS", "Mã phòng học đã tồn tại: " + code);
        }
        Classroom c = new Classroom();
        c.setCode(code);
        c.setBuilding(req.building().trim());
        c.setCapacity(req.capacity());
        c.setRoomType(req.roomType());
        Classroom saved = classroomRepo.save(c);
        return new ClassroomDto(saved.getId(), saved.getCode(), saved.getBuilding(),
                saved.getCapacity(), saved.getRoomType().name(), saved.getStatus().name());
    }

    // --- COURSE SECTIONS ---
    @Transactional(readOnly = true)
    public PageResponse<CourseSectionDto> searchSections(
            Long semesterId, Long courseId, Long lecturerId, String keyword, Pageable pageable) {
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<CourseSection> page = sectionRepo.search(semesterId, courseId, lecturerId, cleanKeyword, pageable);
        return PageResponse.from(page.map(this::toSectionDtoWithSchedules));
    }

    @Transactional(readOnly = true)
    public CourseSectionDto getSectionDetail(Long id) {
        CourseSection cs = sectionRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SECTION_NOT_FOUND", "Không tìm thấy lớp học phần"));
        return toSectionDtoWithSchedules(cs);
    }

    @Transactional
    public CourseSectionDto createCourseSection(CreateCourseSectionRequest req) {
        Course course = courseRepo.findById(req.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("COURSE_NOT_FOUND", "Không tìm thấy học phần"));
        Semester semester = semesterRepo.findById(req.semesterId())
                .orElseThrow(() -> new ResourceNotFoundException("SEMESTER_NOT_FOUND", "Không tìm thấy học kỳ"));

        if (sectionRepo.existsByCourseIdAndSemesterIdAndGroupNumber(course.getId(), semester.getId(), req.groupNumber())) {
            throw new ConflictException("SECTION_GROUP_EXISTS",
                    "Nhóm lớp " + req.groupNumber() + " của môn " + course.getCode() + " trong kỳ " + semester.getCode() + " đã tồn tại");
        }

        Lecturer lecturer = null;
        if (req.lecturerId() != null) {
            lecturer = lecturerRepo.findById(req.lecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("LECTURER_NOT_FOUND", "Không tìm thấy giảng viên"));
        }

        CourseSection section = new CourseSection();
        section.setSectionCode(String.format("%s-%s-%02d", course.getCode(), semester.getCode(), req.groupNumber()));
        section.setCourse(course);
        section.setSemester(semester);
        section.setGroupNumber(req.groupNumber());
        section.setLecturer(lecturer);
        section.setMaxCapacity(req.maxCapacity());

        List<SectionSchedule> candidateSchedules = buildAndValidateSchedules(section, req.schedules(), req.maxCapacity());
        conflictValidator.validateInternalOverlap(candidateSchedules);
        conflictValidator.validateClassroomConflicts(semester.getId(), null, candidateSchedules);
        conflictValidator.validateLecturerConflicts(semester.getId(), req.lecturerId(), null, candidateSchedules);

        section.setSchedules(candidateSchedules);
        CourseSection saved = sectionRepo.save(section);
        return getSectionDetail(saved.getId());
    }

    @Transactional
    public CourseSectionDto updateCourseSection(Long sectionId, UpdateCourseSectionRequest req) {
        CourseSection section = sectionRepo.findWithDetailsById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("SECTION_NOT_FOUND", "Không tìm thấy lớp học phần"));

        if (req.maxCapacity() < section.getCurrentEnrolled()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "CAPACITY_BELOW_ENROLLED",
                    "Không thể giảm sĩ số tối đa (" + req.maxCapacity() + ") xuống thấp hơn số sinh viên đã đăng ký (" + section.getCurrentEnrolled() + ")");
        }

        Lecturer lecturer = null;
        if (req.lecturerId() != null) {
            lecturer = lecturerRepo.findById(req.lecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("LECTURER_NOT_FOUND", "Không tìm thấy giảng viên"));
        }

        List<SectionSchedule> candidateSchedules = buildAndValidateSchedules(section, req.schedules(), req.maxCapacity());
        conflictValidator.validateInternalOverlap(candidateSchedules);
        conflictValidator.validateClassroomConflicts(section.getSemester().getId(), sectionId, candidateSchedules);
        conflictValidator.validateLecturerConflicts(section.getSemester().getId(), req.lecturerId(), sectionId, candidateSchedules);

        section.setLecturer(lecturer);
        section.setMaxCapacity(req.maxCapacity());
        section.setStatus(req.status());
        section.getSchedules().clear();
        sectionRepo.flush();
        section.getSchedules().addAll(candidateSchedules);
        sectionRepo.save(section);

        return getSectionDetail(sectionId);
    }

    /**
     * Xem danh sách sinh viên trong lớp học phần có kiểm tra Data-Scope (BR-03).
     */
    @Transactional(readOnly = true)
    public List<SectionStudentRosterDto> getSectionStudents(UserPrincipal principal, Long sectionId) {
        CourseSection section = sectionRepo.findWithDetailsById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("SECTION_NOT_FOUND", "Không tìm thấy lớp học phần"));

        boolean isPrivileged = principal.hasRole("ADMIN") || principal.hasRole("ACADEMIC_AFFAIRS");
        if (!isPrivileged) {
            boolean isAssignedLecturer = section.getLecturer() != null
                    && section.getLecturer().getUser().getId().equals(principal.getId());
            if (!isAssignedLecturer) {
                throw new DataScopeAccessDeniedException("ACCESS_DENIED_DATA_SCOPE",
                        "Bạn chỉ được phép xem danh sách sinh viên của lớp học phần mình được phân công");
            }
        }

        return enrollmentRepo.findActiveRosterBySectionId(sectionId).stream()
                .map(e -> new SectionStudentRosterDto(
                        e.getId(),
                        e.getStudent().getId(),
                        e.getStudent().getStudentCode(),
                        e.getStudent().getFullName(),
                        e.getStudent().getAcademicClass().getCode(),
                        e.getStudent().getUser().getEmail(),
                        e.getAttemptNumber(),
                        e.getEnrolledAt()))
                .toList();
    }

    /**
     * Xem Thời khóa biểu cá nhân cho Sinh viên hoặc Giảng viên (FR-SCH-01, FR-SCH-02).
     */
    @Transactional(readOnly = true)
    public PersonalTimetableDto getPersonalTimetable(UserPrincipal principal, Long semesterId, Integer weekNumber) {
        Semester semester = semesterRepo.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("SEMESTER_NOT_FOUND", "Không tìm thấy học kỳ"));

        List<TimetableSlotDto> slots = new ArrayList<>();
        String roleCode;

        if (principal.hasRole("STUDENT")) {
            roleCode = "STUDENT";
            Student student = studentRepo.findByUserId(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Không tìm thấy hồ sơ sinh viên"));
            List<Enrollment> enrollments = enrollmentRepo.findActiveByStudentAndSemesterWithDetails(student.getId(), semesterId);
            for (Enrollment e : enrollments) {
                addSlotsForSection(e.getSection(), weekNumber, slots);
            }
        } else if (principal.hasRole("LECTURER")) {
            roleCode = "LECTURER";
            Lecturer lecturer = lecturerRepo.findByUserId(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("LECTURER_NOT_FOUND", "Không tìm thấy hồ sơ giảng viên"));
            List<CourseSection> sections = sectionRepo.findActiveByLecturerAndSemester(lecturer.getId(), semesterId);
            for (CourseSection s : sections) {
                addSlotsForSection(s, weekNumber, slots);
            }
        } else {
            throw new DataScopeAccessDeniedException("TIMETABLE_ROLE_NOT_SUPPORTED",
                    "Chỉ Sinh viên và Giảng viên có thời khóa biểu cá nhân");
        }

        return new PersonalTimetableDto(semester.getId(), semester.getCode(), semester.getName(), roleCode, slots);
    }

    private void addSlotsForSection(CourseSection s, Integer weekNumber, List<TimetableSlotDto> out) {
        List<SectionSchedule> schedules = scheduleRepo.findBySectionIdWithClassroom(s.getId());
        String lecturerName = s.getLecturer() != null ? s.getLecturer().getFullName() : "Chưa phân công";
        for (SectionSchedule sch : schedules) {
            if (weekNumber != null && (weekNumber < sch.getStartWeek() || weekNumber > sch.getEndWeek())) {
                continue;
            }
            out.add(new TimetableSlotDto(
                    s.getId(),
                    s.getSectionCode(),
                    s.getCourse().getCode(),
                    s.getCourse().getName(),
                    s.getCourse().getCredits(),
                    lecturerName,
                    sch.getDayOfWeek(),
                    sch.getStartPeriod(),
                    sch.getEndPeriod(),
                    sch.getStartWeek(),
                    sch.getEndWeek(),
                    sch.getClassroom().getCode(),
                    sch.getClassroom().getBuilding()
            ));
        }
    }

    private List<SectionSchedule> buildAndValidateSchedules(
            CourseSection section, List<ScheduleItemRequest> items, int sectionCapacity) {
        List<SectionSchedule> result = new ArrayList<>();
        for (ScheduleItemRequest item : items) {
            if (item.endPeriod() < item.startPeriod()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_PERIOD_RANGE",
                        "Tiết kết thúc phải lớn hơn hoặc bằng tiết bắt đầu");
            }
            if (item.endWeek() < item.startWeek()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_WEEK_RANGE",
                        "Tuần kết thúc phải lớn hơn hoặc bằng tuần bắt đầu");
            }
            Classroom room = classroomRepo.findById(item.classroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Không tìm thấy phòng học"));
            if (sectionCapacity > room.getCapacity()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "CLASSROOM_CAPACITY_EXCEEDED",
                        "Sĩ số tối đa của lớp (" + sectionCapacity + ") vượt quá sức chứa phòng " + room.getCode() + " (" + room.getCapacity() + ")");
            }
            SectionSchedule sch = new SectionSchedule();
            sch.setSection(section);
            sch.setDayOfWeek(item.dayOfWeek());
            sch.setStartPeriod(item.startPeriod());
            sch.setEndPeriod(item.endPeriod());
            sch.setStartWeek(item.startWeek());
            sch.setEndWeek(item.endWeek());
            sch.setClassroom(room);
            result.add(sch);
        }
        return result;
    }

    private SemesterDto toSemesterDto(Semester s) {
        return new SemesterDto(
                s.getId(), s.getCode(), s.getName(),
                s.getAcademicYear().getId(), s.getAcademicYear().getCode(),
                s.getTermType().name(), s.getStartDate(), s.getEndDate(),
                s.getMinCredits(), s.getMaxCredits(),
                s.getRegistrationStartAt(), s.getRegistrationEndAt(),
                s.getIsRegistrationOpen(), s.getStatus().name()
        );
    }

    private CourseSectionDto toSectionDtoWithSchedules(CourseSection cs) {
        List<ScheduleItemDto> schDtos = scheduleRepo.findBySectionIdWithClassroom(cs.getId()).stream()
                .map(sch -> new ScheduleItemDto(
                        sch.getId(), sch.getDayOfWeek(), sch.getStartPeriod(), sch.getEndPeriod(),
                        sch.getStartWeek(), sch.getEndWeek(),
                        sch.getClassroom().getId(), sch.getClassroom().getCode(), sch.getClassroom().getBuilding()))
                .toList();

        return new CourseSectionDto(
                cs.getId(), cs.getSectionCode(),
                cs.getCourse().getId(), cs.getCourse().getCode(), cs.getCourse().getName(), cs.getCourse().getCredits(),
                cs.getSemester().getId(), cs.getSemester().getCode(), cs.getGroupNumber(),
                cs.getLecturer() != null ? cs.getLecturer().getId() : null,
                cs.getLecturer() != null ? cs.getLecturer().getFullName() : null,
                cs.getMaxCapacity(), cs.getCurrentEnrolled(),
                cs.getGradeStatus().name(), cs.getStatus().name(), schDtos
        );
    }
}
