package com.ptit.training.grade.service;

import com.ptit.training.auth.entity.User;
import com.ptit.training.auth.repository.UserRepository;
import com.ptit.training.common.ApiError;
import com.ptit.training.common.exception.BusinessException;
import com.ptit.training.common.exception.DataScopeAccessDeniedException;
import com.ptit.training.common.exception.ResourceNotFoundException;
import com.ptit.training.common.security.UserPrincipal;
import com.ptit.training.course.entity.GradeComponent;
import com.ptit.training.course.repository.GradeComponentRepository;
import com.ptit.training.enrollment.entity.Enrollment;
import com.ptit.training.enrollment.repository.EnrollmentRepository;
import com.ptit.training.grade.dto.GradeDtos.*;
import com.ptit.training.grade.entity.GradingScale;
import com.ptit.training.grade.entity.StudentGrade;
import com.ptit.training.grade.entity.SystemConfig;
import com.ptit.training.grade.repository.GradingScaleRepository;
import com.ptit.training.grade.repository.StudentGradeRepository;
import com.ptit.training.grade.repository.SystemConfigRepository;
import com.ptit.training.reporting.service.AuditLogService;
import com.ptit.training.section.entity.CourseSection;
import com.ptit.training.section.entity.Semester;
import com.ptit.training.section.repository.CourseSectionRepository;
import com.ptit.training.student.entity.Student;
import com.ptit.training.student.repository.StudentRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Quản lý bảng điểm lớp học phần (DRAFT -> PUBLISHED -> LOCKED), import CSV, sửa điểm sau khóa & tra cứu GPA/CPA.
 */
@Service
public class GradeService {

    private final CourseSectionRepository sectionRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final StudentGradeRepository studentGradeRepo;
    private final GradeComponentRepository componentRepo;
    private final GradingScaleRepository scaleRepo;
    private final SystemConfigRepository configRepo;
    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private final GradeCalculatorDomainService calculator;
    private final AuditLogService auditLogService;

    public GradeService(
            CourseSectionRepository sectionRepo,
            EnrollmentRepository enrollmentRepo,
            StudentGradeRepository studentGradeRepo,
            GradeComponentRepository componentRepo,
            GradingScaleRepository scaleRepo,
            SystemConfigRepository configRepo,
            StudentRepository studentRepo,
            UserRepository userRepo,
            GradeCalculatorDomainService calculator,
            AuditLogService auditLogService) {
        this.sectionRepo = sectionRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.studentGradeRepo = studentGradeRepo;
        this.componentRepo = componentRepo;
        this.scaleRepo = scaleRepo;
        this.configRepo = configRepo;
        this.studentRepo = studentRepo;
        this.userRepo = userRepo;
        this.calculator = calculator;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public SectionGradeBookDto getSectionGradeBook(UserPrincipal principal, Long sectionId) {
        CourseSection section = sectionRepo.findWithDetailsById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("SECTION_NOT_FOUND", "Không tìm thấy lớp học phần"));

        verifySectionAccess(principal, section);

        List<GradeComponent> components = componentRepo.findByCourseIdOrderByDisplayOrderAsc(section.getCourse().getId());
        List<Enrollment> roster = enrollmentRepo.findActiveRosterBySectionId(sectionId);
        List<StudentGrade> allGrades = studentGradeRepo.findBySectionIdWithComponent(sectionId);

        Map<Long, Map<Long, BigDecimal>> scoresByEnrollment = new HashMap<>();
        for (StudentGrade sg : allGrades) {
            scoresByEnrollment
                    .computeIfAbsent(sg.getEnrollment().getId(), k -> new LinkedHashMap<>())
                    .put(sg.getGradeComponent().getId(), sg.getScore());
        }

        List<ComponentHeaderDto> compDtos = components.stream()
                .map(c -> new ComponentHeaderDto(c.getId(), c.getCode(), c.getName(),
                        c.getWeightPercent(), c.getIsFinalExam(), c.getMinThreshold()))
                .toList();

        List<StudentGradeRowDto> rows = roster.stream()
                .map(e -> new StudentGradeRowDto(
                        e.getId(),
                        e.getStudent().getId(),
                        e.getStudent().getStudentCode(),
                        e.getStudent().getFullName(),
                        e.getStudent().getAcademicClass().getCode(),
                        scoresByEnrollment.getOrDefault(e.getId(), Map.of()),
                        e.getFinalScore10(),
                        e.getLetterGrade(),
                        e.getGpaScore4(),
                        e.getIsPassed()
                ))
                .toList();

        return new SectionGradeBookDto(
                section.getId(),
                section.getSectionCode(),
                section.getCourse().getCode(),
                section.getCourse().getName(),
                section.getGradeStatus().name(),
                section.getPublishedAt(),
                section.getLockedAt(),
                compDtos,
                rows
        );
    }

    @Transactional
    public SectionGradeBookDto saveSectionGrades(UserPrincipal principal, Long sectionId, SaveSectionGradesRequest req) {
        CourseSection section = sectionRepo.findWithDetailsById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("SECTION_NOT_FOUND", "Không tìm thấy lớp học phần"));

        verifySectionAccess(principal, section);

        if (section.getGradeStatus() != CourseSection.GradeBookStatus.DRAFT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "GRADE_ALREADY_PUBLISHED_OR_LOCKED",
                    "Bảng điểm đã được công bố hoặc khóa sổ, giảng viên không thể tự chỉnh sửa");
        }

        List<GradeComponent> components = componentRepo.findByCourseIdOrderByDisplayOrderAsc(section.getCourse().getId());
        Map<Long, GradeComponent> componentById = components.stream()
                .collect(Collectors.toMap(GradeComponent::getId, c -> c));
        List<GradingScale> scales = scaleRepo.findAllByOrderByDisplayOrderAsc();
        User actor = userRepo.getReferenceById(principal.getId());

        for (StudentGradeRowInput row : req.rows()) {
            Enrollment enrollment = enrollmentRepo.findWithDetailsById(row.enrollmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("ENROLLMENT_NOT_FOUND",
                            "Không tìm thấy lượt đăng ký ID=" + row.enrollmentId()));

            if (!enrollment.getSection().getId().equals(sectionId) || enrollment.getStatus() != Enrollment.Status.ENROLLED) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "ENROLLMENT_SECTION_MISMATCH",
                        "Lượt đăng ký ID=" + row.enrollmentId() + " không thuộc lớp học phần này");
            }

            upsertGradesAndRecompute(enrollment, row.scores(), componentById, components, scales, actor);
        }

        return getSectionGradeBook(principal, sectionId);
    }

    /**
     * Import bảng điểm lớp học phần từ file CSV theo nguyên tắc Validate & Preview + All-or-Nothing (FR-GRD-02, BR-32).
     * Định dạng cột: studentCode, <componentCode1>, <componentCode2>, ...
     */
    @Transactional
    public ImportGradeResultDto importSectionGradesCsv(UserPrincipal principal, Long sectionId, MultipartFile file, boolean dryRun) {
        CourseSection section = sectionRepo.findWithDetailsById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("SECTION_NOT_FOUND", "Không tìm thấy lớp học phần"));

        verifySectionAccess(principal, section);
        if (section.getGradeStatus() != CourseSection.GradeBookStatus.DRAFT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "GRADE_ALREADY_PUBLISHED_OR_LOCKED",
                    "Bảng điểm đã công bố hoặc khóa sổ");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "IMPORT_EMPTY_FILE", "File CSV rỗng");
        }

        List<GradeComponent> components = componentRepo.findByCourseIdOrderByDisplayOrderAsc(section.getCourse().getId());
        Map<String, GradeComponent> compByCode = components.stream()
                .collect(Collectors.toMap(c -> c.getCode().toUpperCase(), c -> c));
        Map<Long, GradeComponent> compById = components.stream()
                .collect(Collectors.toMap(GradeComponent::getId, c -> c));

        Map<String, Enrollment> rosterByStudentCode = enrollmentRepo.findActiveRosterBySectionId(sectionId).stream()
                .collect(Collectors.toMap(e -> e.getStudent().getStudentCode().toUpperCase(), e -> e));

        List<ImportGradeErrorRow> errors = new ArrayList<>();
        List<StudentGradeRowInput> validInputs = new ArrayList<>();
        int totalRows = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "IMPORT_EMPTY_FILE", "File thiếu dòng tiêu đề");
            }
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }
            String[] headers = headerLine.split(",", -1);
            if (headers.length < 2) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "IMPORT_INVALID_HEADER",
                        "Dòng tiêu đề phải bắt đầu bằng studentCode followed by mã thành phần điểm");
            }

            List<GradeComponent> headerComponents = new ArrayList<>();
            for (int i = 1; i < headers.length; i++) {
                String compCode = headers[i].trim().toUpperCase();
                GradeComponent gc = compByCode.get(compCode);
                if (gc == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "IMPORT_UNKNOWN_COMPONENT",
                            "Mã thành phần điểm không hợp lệ ở cột " + (i + 1) + ": " + compCode);
                }
                headerComponents.add(gc);
            }

            String line;
            int rowNum = 1;
            Set<String> seenStudents = new HashSet<>();
            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (line.isBlank()) continue;
                totalRows++;

                String[] cols = line.split(",", -1);
                String studentCode = cols[0].trim().toUpperCase();
                Enrollment enrollment = rosterByStudentCode.get(studentCode);
                if (enrollment == null) {
                    errors.add(new ImportGradeErrorRow(rowNum, studentCode, "Sinh viên không nằm trong danh sách lớp học phần"));
                    continue;
                }
                if (!seenStudents.add(studentCode)) {
                    errors.add(new ImportGradeErrorRow(rowNum, studentCode, "Trùng mã sinh viên trong file import"));
                    continue;
                }

                List<ComponentScoreInput> scores = new ArrayList<>();
                boolean rowFailed = false;
                for (int i = 0; i < headerComponents.size(); i++) {
                    if (i + 1 >= cols.length || cols[i + 1].isBlank()) {
                        continue;
                    }
                    try {
                        BigDecimal val = new BigDecimal(cols[i + 1].trim()).setScale(1, RoundingMode.HALF_UP);
                        if (val.compareTo(BigDecimal.ZERO) < 0 || val.compareTo(BigDecimal.TEN) > 0) {
                            errors.add(new ImportGradeErrorRow(rowNum, studentCode,
                                    "Điểm " + headerComponents.get(i).getCode() + " ngoài khoảng [0.0, 10.0]"));
                            rowFailed = true;
                            break;
                        }
                        scores.add(new ComponentScoreInput(headerComponents.get(i).getId(), val));
                    } catch (NumberFormatException ex) {
                        errors.add(new ImportGradeErrorRow(rowNum, studentCode,
                                "Điểm không phải số hợp lệ tại cột " + headerComponents.get(i).getCode()));
                        rowFailed = true;
                        break;
                    }
                }
                if (!rowFailed && !scores.isEmpty()) {
                    validInputs.add(new StudentGradeRowInput(enrollment.getId(), scores));
                }
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "IMPORT_READ_ERROR", "Lỗi đọc file: " + ex.getMessage());
        }

        if (!errors.isEmpty()) {
            if (!dryRun) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "IMPORT_VALIDATION_FAILED",
                        "File import có " + errors.size() + " dòng lỗi. Đã hủy toàn bộ giao dịch.",
                        errors.stream().map(e -> new ApiError.FieldIssue(
                                "row-" + e.rowNumber(), e.studentCode() + ": " + e.message())).toList());
            }
            return new ImportGradeResultDto(true, totalRows, validInputs.size(), 0, errors);
        }

        if (dryRun) {
            return new ImportGradeResultDto(true, totalRows, validInputs.size(), 0, List.of());
        }

        List<GradingScale> scales = scaleRepo.findAllByOrderByDisplayOrderAsc();
        User actor = userRepo.getReferenceById(principal.getId());
        for (StudentGradeRowInput input : validInputs) {
            Enrollment e = enrollmentRepo.findWithDetailsById(input.enrollmentId()).orElseThrow();
            upsertGradesAndRecompute(e, input.scores(), compById, components, scales, actor);
        }
        return new ImportGradeResultDto(false, totalRows, validInputs.size(), validInputs.size(), List.of());
    }

    @Transactional
    public SectionGradeBookDto publishGrades(UserPrincipal principal, Long sectionId) {
        CourseSection section = sectionRepo.findWithDetailsById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("SECTION_NOT_FOUND", "Không tìm thấy lớp học phần"));

        verifySectionAccess(principal, section);
        if (section.getGradeStatus() != CourseSection.GradeBookStatus.DRAFT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "GRADE_ALREADY_PUBLISHED_OR_LOCKED",
                    "Bảng điểm lớp đã được công bố trước đó");
        }

        List<Enrollment> roster = enrollmentRepo.findActiveRosterBySectionId(sectionId);
        for (Enrollment e : roster) {
            if (e.getFinalScore10() == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "GRADE_INCOMPLETE_FOR_PUBLISH",
                        "Sinh viên " + e.getStudent().getStudentCode() + " chưa có đủ đầu điểm thành phần để công bố");
            }
        }

        section.setGradeStatus(CourseSection.GradeBookStatus.PUBLISHED);
        section.setPublishedAt(Instant.now());
        sectionRepo.save(section);

        auditLogService.record(principal.getId(), "PUBLISH_GRADES", "COURSE_SECTION", String.valueOf(sectionId),
                Map.of("gradeStatus", "DRAFT"), Map.of("gradeStatus", "PUBLISHED"),
                "Công bố bảng điểm lớp " + section.getSectionCode());

        return getSectionGradeBook(principal, sectionId);
    }

    @Transactional
    public SectionGradeBookDto lockGrades(UserPrincipal principal, Long sectionId) {
        CourseSection section = sectionRepo.findWithDetailsById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("SECTION_NOT_FOUND", "Không tìm thấy lớp học phần"));

        if (section.getGradeStatus() != CourseSection.GradeBookStatus.PUBLISHED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "GRADE_MUST_BE_PUBLISHED_FIRST",
                    "Chỉ có thể khóa sổ bảng điểm sau khi bảng điểm đã ở trạng thái PUBLISHED");
        }

        section.setGradeStatus(CourseSection.GradeBookStatus.LOCKED);
        section.setLockedAt(Instant.now());
        sectionRepo.save(section);

        auditLogService.record(principal.getId(), "LOCK_GRADES", "COURSE_SECTION", String.valueOf(sectionId),
                Map.of("gradeStatus", "PUBLISHED"), Map.of("gradeStatus", "LOCKED"),
                "Khóa sổ bảng điểm lớp " + section.getSectionCode());

        return getSectionGradeBook(principal, sectionId);
    }

    /**
     * Phòng đào tạo sửa điểm sau khi đã công bố hoặc khóa sổ (bắt buộc nhập lý do & ghi AuditLog - BR-30).
     */
    @Transactional
    public StudentGradeRowDto overrideScore(UserPrincipal aaPrincipal, Long enrollmentId, OverrideScoreRequest req) {
        Enrollment enrollment = enrollmentRepo.findWithDetailsById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("ENROLLMENT_NOT_FOUND", "Không tìm thấy lượt đăng ký"));

        CourseSection section = enrollment.getSection();
        List<GradeComponent> components = componentRepo.findByCourseIdOrderByDisplayOrderAsc(section.getCourse().getId());
        Map<Long, GradeComponent> componentById = components.stream()
                .collect(Collectors.toMap(GradeComponent::getId, c -> c));
        List<GradingScale> scales = scaleRepo.findAllByOrderByDisplayOrderAsc();

        BigDecimal oldFinal = enrollment.getFinalScore10();
        String oldLetter = enrollment.getLetterGrade();

        User actor = userRepo.getReferenceById(aaPrincipal.getId());
        Map<Long, BigDecimal> scoreMap = upsertGradesAndRecompute(enrollment, req.scores(), componentById, components, scales, actor);

        auditLogService.record(aaPrincipal.getId(), "OVERRIDE_SCORE", "ENROLLMENT", String.valueOf(enrollmentId),
                Map.of("finalScore10", oldFinal != null ? oldFinal.toString() : "null", "letterGrade", String.valueOf(oldLetter)),
                Map.of("finalScore10", String.valueOf(enrollment.getFinalScore10()), "letterGrade", String.valueOf(enrollment.getLetterGrade())),
                req.reason().trim());

        return new StudentGradeRowDto(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getStudentCode(),
                enrollment.getStudent().getFullName(),
                enrollment.getStudent().getAcademicClass().getCode(),
                scoreMap,
                enrollment.getFinalScore10(),
                enrollment.getLetterGrade(),
                enrollment.getGpaScore4(),
                enrollment.getIsPassed()
        );
    }

    @Transactional(readOnly = true)
    public AcademicTranscriptDto getMyTranscript(Long currentUserId) {
        Student student = studentRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Không tìm thấy hồ sơ sinh viên"));
        return buildTranscript(student);
    }

    @Transactional(readOnly = true)
    public AcademicTranscriptDto getStudentTranscript(Long studentId) {
        Student student = studentRepo.findWithDetailsById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Không tìm thấy hồ sơ sinh viên"));
        return buildTranscript(student);
    }

    @Transactional(readOnly = true)
    public List<GradingScaleDto> listGradingScales() {
        return scaleRepo.findAllByOrderByDisplayOrderAsc().stream()
                .map(gs -> new GradingScaleDto(gs.getId(), gs.getMinScore10(), gs.getMaxScore10(),
                        gs.getLetterGrade(), gs.getGpaScore4(), gs.getIsPass(), gs.getDisplayOrder()))
                .toList();
    }

    @Transactional
    public List<GradingScaleDto> updateGradingScales(Long actorUserId, UpdateGradingScalesRequest req) {
        List<GradingScaleDto> sorted = req.scales().stream()
                .sorted(Comparator.comparing(GradingScaleDto::minScore10))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            GradingScaleDto curr = sorted.get(i);
            if (curr.maxScore10().compareTo(curr.minScore10()) < 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_SCALE_RANGE",
                        "Điểm tối đa phải >= điểm tối thiểu tại mức " + curr.letterGrade());
            }
            if (i > 0 && curr.minScore10().compareTo(sorted.get(i - 1).maxScore10()) <= 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "OVERLAPPING_GRADING_SCALE",
                        "Các khoảng điểm trong thang điểm không được chồng lấn nhau");
            }
        }

        scaleRepo.deleteAll();
        scaleRepo.flush();
        for (GradingScaleDto dto : req.scales()) {
            GradingScale gs = new GradingScale();
            gs.setMinScore10(dto.minScore10());
            gs.setMaxScore10(dto.maxScore10());
            gs.setLetterGrade(dto.letterGrade().trim().toUpperCase());
            gs.setGpaScore4(dto.gpaScore4());
            gs.setIsPass(dto.isPass());
            gs.setDisplayOrder(dto.displayOrder());
            scaleRepo.save(gs);
        }

        auditLogService.record(actorUserId, "UPDATE_GRADING_SCALES", "SYSTEM_CONFIG", "GRADING_SCALES",
                null, Map.of("count", req.scales().size()), "Cập nhật thang điểm quy đổi");

        return listGradingScales();
    }

    @Transactional(readOnly = true)
    public List<SystemConfigDto> listSystemConfigs() {
        return configRepo.findAll().stream()
                .map(c -> new SystemConfigDto(c.getConfigKey(), c.getConfigValue(), c.getValueType(), c.getDescription()))
                .toList();
    }

    @Transactional
    public SystemConfigDto updateSystemConfig(Long actorUserId, String key, String value) {
        SystemConfig cfg = configRepo.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("CONFIG_NOT_FOUND", "Không tìm thấy tham số: " + key));
        String oldVal = cfg.getConfigValue();
        cfg.setConfigValue(value.trim());
        cfg.setUpdatedBy(userRepo.getReferenceById(actorUserId));
        SystemConfig saved = configRepo.save(cfg);

        auditLogService.record(actorUserId, "UPDATE_SYSTEM_CONFIG", "SYSTEM_CONFIG", key,
                Map.of("value", oldVal), Map.of("value", value.trim()), "Cập nhật tham số quy chế");

        return new SystemConfigDto(saved.getConfigKey(), saved.getConfigValue(), saved.getValueType(), saved.getDescription());
    }

    private Map<Long, BigDecimal> upsertGradesAndRecompute(
            Enrollment enrollment,
            List<ComponentScoreInput> inputs,
            Map<Long, GradeComponent> componentById,
            List<GradeComponent> allComponents,
            List<GradingScale> scales,
            User actor) {

        for (ComponentScoreInput input : inputs) {
            GradeComponent comp = componentById.get(input.gradeComponentId());
            if (comp == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_GRADE_COMPONENT",
                        "Thành phần điểm ID=" + input.gradeComponentId() + " không thuộc học phần này");
            }
            BigDecimal normalized = input.score().setScale(1, RoundingMode.HALF_UP);
            StudentGrade sg = studentGradeRepo
                    .findByEnrollmentIdAndGradeComponentId(enrollment.getId(), comp.getId())
                    .orElseGet(() -> {
                        StudentGrade n = new StudentGrade();
                        n.setEnrollment(enrollment);
                        n.setGradeComponent(comp);
                        return n;
                    });
            sg.setScore(normalized);
            sg.setUpdatedBy(actor);
            studentGradeRepo.save(sg);
        }

        List<StudentGrade> savedGrades = studentGradeRepo.findByEnrollmentIdWithComponent(enrollment.getId());
        Map<Long, BigDecimal> scoreMap = new LinkedHashMap<>();
        for (StudentGrade sg : savedGrades) {
            scoreMap.put(sg.getGradeComponent().getId(), sg.getScore());
        }

        GradeCalculatorDomainService.GradeCalculationResult calc =
                calculator.computeFinalGrade(allComponents, scoreMap, scales);
        if (calc != null) {
            enrollment.setFinalScore10(calc.finalScore10());
            enrollment.setLetterGrade(calc.letterGrade());
            enrollment.setGpaScore4(calc.gpaScore4());
            enrollment.setIsPassed(calc.isPassed());
            enrollmentRepo.save(enrollment);
        }
        return scoreMap;
    }

    private AcademicTranscriptDto buildTranscript(Student student) {
        List<Enrollment> enrollments = enrollmentRepo.findAllEnrolledByStudentIdWithDetails(student.getId());
        boolean useHighest = configRepo.findById("RETAKE_GPA_POLICY")
                .map(c -> !"LATEST_SCORE".equalsIgnoreCase(c.getConfigValue()))
                .orElse(true);

        GradeCalculatorDomainService.CpaSummary cpaSummary = calculator.computeCumulativeCpa(enrollments, useHighest);

        Map<Long, Semester> semesterMap = new LinkedHashMap<>();
        Map<Long, List<Enrollment>> bySemester = new LinkedHashMap<>();
        for (Enrollment e : enrollments) {
            semesterMap.put(e.getSemester().getId(), e.getSemester());
            bySemester.computeIfAbsent(e.getSemester().getId(), k -> new ArrayList<>()).add(e);
        }

        List<SemesterTranscriptDto> semesterDtos = new ArrayList<>();
        for (Map.Entry<Long, List<Enrollment>> entry : bySemester.entrySet()) {
            Semester sem = semesterMap.get(entry.getKey());
            List<Enrollment> list = entry.getValue();
            int regCredits = list.stream().mapToInt(e -> e.getCourse().getCredits()).sum();

            List<Enrollment> publishedInSem = list.stream()
                    .filter(e -> e.getSection().getGradeStatus() != CourseSection.GradeBookStatus.DRAFT)
                    .toList();
            BigDecimal semGpa = calculator.computeSemesterGpa4(publishedInSem);

            List<CourseTranscriptItemDto> courseItems = new ArrayList<>();
            for (Enrollment e : list) {
                boolean visible = e.getSection().getGradeStatus() != CourseSection.GradeBookStatus.DRAFT;
                Map<String, BigDecimal> compScores = new LinkedHashMap<>();
                if (visible) {
                    for (StudentGrade sg : studentGradeRepo.findByEnrollmentIdWithComponent(e.getId())) {
                        compScores.put(sg.getGradeComponent().getName(), sg.getScore());
                    }
                }
                courseItems.add(new CourseTranscriptItemDto(
                        e.getId(),
                        e.getSection().getSectionCode(),
                        e.getCourse().getCode(),
                        e.getCourse().getName(),
                        e.getCourse().getCredits(),
                        e.getAttemptNumber(),
                        e.getSection().getGradeStatus().name(),
                        compScores,
                        visible ? e.getFinalScore10() : null,
                        visible ? e.getLetterGrade() : null,
                        visible ? e.getGpaScore4() : null,
                        visible ? e.getIsPassed() : null
                ));
            }

            semesterDtos.add(new SemesterTranscriptDto(
                    sem.getId(), sem.getCode(), sem.getName(), regCredits, semGpa, courseItems));
        }

        return new AcademicTranscriptDto(
                student.getId(),
                student.getStudentCode(),
                student.getFullName(),
                student.getMajor().getName(),
                student.getAcademicClass().getCode(),
                cpaSummary.cpa4(),
                cpaSummary.accumulatedCredits(),
                semesterDtos
        );
    }

    private void verifySectionAccess(UserPrincipal principal, CourseSection section) {
        if (principal.hasRole("ADMIN") || principal.hasRole("ACADEMIC_AFFAIRS")) {
            return;
        }
        boolean isAssignedLecturer = section.getLecturer() != null
                && section.getLecturer().getUser().getId().equals(principal.getId());
        if (!isAssignedLecturer) {
            throw new DataScopeAccessDeniedException("GRADE_NOT_ASSIGNED_LECTURER",
                    "Bạn không được phân công giảng dạy lớp học phần " + section.getSectionCode());
        }
    }
}
