package com.ptit.training.student.service;

import com.ptit.training.auth.entity.Role;
import com.ptit.training.auth.entity.User;
import com.ptit.training.auth.repository.RoleRepository;
import com.ptit.training.auth.repository.UserRepository;
import com.ptit.training.common.PageResponse;
import com.ptit.training.common.exception.BusinessException;
import com.ptit.training.common.exception.ConflictException;
import com.ptit.training.common.exception.ResourceNotFoundException;
import com.ptit.training.organization.entity.AcademicClass;
import com.ptit.training.organization.entity.Major;
import com.ptit.training.organization.repository.AcademicClassRepository;
import com.ptit.training.organization.repository.MajorRepository;
import com.ptit.training.reporting.service.AuditLogService;
import com.ptit.training.student.dto.StudentDtos.*;
import com.ptit.training.student.entity.Student;
import com.ptit.training.student.entity.StudentStatusHistory;
import com.ptit.training.student.repository.StudentRepository;
import com.ptit.training.student.repository.StudentStatusHistoryRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Dịch vụ quản lý hồ sơ sinh viên, vòng đời trạng thái học vụ, tự cập nhật liên lạc và import CSV.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepo;
    private final StudentStatusHistoryRepository historyRepo;
    private final AcademicClassRepository classRepo;
    private final MajorRepository majorRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public StudentService(
            StudentRepository studentRepo,
            StudentStatusHistoryRepository historyRepo,
            AcademicClassRepository classRepo,
            MajorRepository majorRepo,
            UserRepository userRepo,
            RoleRepository roleRepo,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {
        this.studentRepo = studentRepo;
        this.historyRepo = historyRepo;
        this.classRepo = classRepo;
        this.majorRepo = majorRepo;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentSummaryDto> searchStudents(
            String keyword, Long majorId, Long academicClassId, Integer cohortYear,
            Student.AcademicStatus status, Pageable pageable) {
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Student> page = studentRepo.search(cleanKeyword, majorId, academicClassId, cohortYear, status, pageable);
        return PageResponse.from(page.map(this::toSummaryDto));
    }

    @Transactional(readOnly = true)
    public StudentDetailDto getStudentById(Long id) {
        Student s = studentRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Không tìm thấy hồ sơ sinh viên"));
        return toDetailDto(s);
    }

    @Transactional(readOnly = true)
    public StudentDetailDto getMyProfile(Long currentUserId) {
        Student s = studentRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Tài khoản chưa gắn hồ sơ sinh viên"));
        return toDetailDto(s);
    }

    @Transactional
    public StudentDetailDto createStudent(Long actorUserId, CreateStudentRequest req) {
        String code = req.studentCode().trim().toUpperCase();
        String email = req.email().trim().toLowerCase();
        String nationalId = req.nationalId().trim();

        if (studentRepo.existsByStudentCodeIgnoreCase(code) || userRepo.existsByUsernameIgnoreCase(code)) {
            throw new ConflictException("STUDENT_CODE_EXISTS", "Mã sinh viên đã tồn tại: " + code);
        }
        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("EMAIL_EXISTS", "Email đã tồn tại: " + email);
        }
        if (studentRepo.existsByNationalIdIgnoreCase(nationalId)) {
            throw new ConflictException("NATIONAL_ID_EXISTS", "Số CCCD đã tồn tại trong hệ thống: " + nationalId);
        }

        AcademicClass academicClass = classRepo.findById(req.academicClassId())
                .orElseThrow(() -> new ResourceNotFoundException("ACADEMIC_CLASS_NOT_FOUND", "Không tìm thấy lớp hành chính"));
        Major major = majorRepo.findById(req.majorId())
                .orElseThrow(() -> new ResourceNotFoundException("MAJOR_NOT_FOUND", "Không tìm thấy ngành đào tạo"));

        if (!academicClass.getMajor().getId().equals(major.getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "CLASS_MAJOR_MISMATCH",
                    "Lớp hành chính " + academicClass.getCode() + " không thuộc ngành " + major.getName());
        }

        Role studentRole = roleRepo.findByCode("STUDENT")
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_NOT_FOUND", "Thiếu vai trò STUDENT"));

        String rawPassword = (req.initialPassword() != null && !req.initialPassword().isBlank())
                ? req.initialPassword()
                : "Ptit@12345";

        User user = new User();
        user.setUsername(code);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(User.Status.ACTIVE);
        user.setRoles(Set.of(studentRole));
        User savedUser = userRepo.save(user);

        Student student = new Student();
        student.setUser(savedUser);
        student.setStudentCode(code);
        student.setFullName(req.fullName().trim());
        student.setDateOfBirth(req.dateOfBirth());
        student.setGender(req.gender());
        student.setNationalId(nationalId);
        student.setPersonalEmail(req.personalEmail());
        student.setPhone(req.phone());
        student.setAddress(req.address());
        student.setAcademicClass(academicClass);
        student.setMajor(major);
        student.setCohortYear(req.cohortYear());
        student.setAcademicStatus(Student.AcademicStatus.ACTIVE);
        Student saved = studentRepo.save(student);

        StudentStatusHistory history = new StudentStatusHistory();
        history.setStudent(saved);
        history.setOldStatus(null);
        history.setNewStatus(Student.AcademicStatus.ACTIVE);
        history.setReason("Khởi tạo hồ sơ sinh viên");
        history.setChangedBy(userRepo.getReferenceById(actorUserId));
        historyRepo.save(history);

        return getStudentById(saved.getId());
    }

    @Transactional
    public StudentDetailDto updateStudent(Long id, UpdateStudentRequest req) {
        Student s = studentRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Không tìm thấy hồ sơ sinh viên"));

        String nationalId = req.nationalId().trim();
        if (!s.getNationalId().equalsIgnoreCase(nationalId) && studentRepo.existsByNationalIdIgnoreCase(nationalId)) {
            throw new ConflictException("NATIONAL_ID_EXISTS", "Số CCCD đã thuộc về sinh viên khác");
        }

        AcademicClass academicClass = classRepo.findById(req.academicClassId())
                .orElseThrow(() -> new ResourceNotFoundException("ACADEMIC_CLASS_NOT_FOUND", "Không tìm thấy lớp hành chính"));
        Major major = majorRepo.findById(req.majorId())
                .orElseThrow(() -> new ResourceNotFoundException("MAJOR_NOT_FOUND", "Không tìm thấy ngành đào tạo"));

        if (!academicClass.getMajor().getId().equals(major.getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "CLASS_MAJOR_MISMATCH",
                    "Lớp hành chính không thuộc ngành đào tạo đã chọn");
        }

        s.setFullName(req.fullName().trim());
        s.setDateOfBirth(req.dateOfBirth());
        s.setGender(req.gender());
        s.setNationalId(nationalId);
        s.setPersonalEmail(req.personalEmail());
        s.setPhone(req.phone());
        s.setAddress(req.address());
        s.setAcademicClass(academicClass);
        s.setMajor(major);
        s.setCohortYear(req.cohortYear());
        studentRepo.save(s);

        return toDetailDto(s);
    }

    @Transactional
    public StudentDetailDto updateAcademicStatus(Long actorUserId, Long studentId, UpdateAcademicStatusRequest req) {
        Student s = studentRepo.findWithDetailsById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Không tìm thấy hồ sơ sinh viên"));

        Student.AcademicStatus oldStatus = s.getAcademicStatus();
        if (oldStatus == req.newStatus()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "STATUS_UNCHANGED",
                    "Trạng thái mới trùng với trạng thái hiện tại");
        }

        s.setAcademicStatus(req.newStatus());
        studentRepo.save(s);

        StudentStatusHistory history = new StudentStatusHistory();
        history.setStudent(s);
        history.setOldStatus(oldStatus);
        history.setNewStatus(req.newStatus());
        history.setReason(req.reason().trim());
        history.setChangedBy(userRepo.getReferenceById(actorUserId));
        historyRepo.save(history);

        auditLogService.record(actorUserId, "CHANGE_STUDENT_ACADEMIC_STATUS", "STUDENT", String.valueOf(studentId),
                Map.of("academicStatus", oldStatus.name()),
                Map.of("academicStatus", req.newStatus().name()),
                req.reason().trim());

        return toDetailDto(s);
    }

    /**
     * Sinh viên tự cập nhật thông tin liên lạc — chỉ cho phép 4 trường (BR-09).
     */
    @Transactional
    public StudentDetailDto updateSelfContact(Long currentUserId, UpdateSelfContactRequest req) {
        Student s = studentRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Không tìm thấy hồ sơ sinh viên"));
        s.setPhone(req.phone());
        s.setPersonalEmail(req.personalEmail());
        s.setAddress(req.address());
        s.setAvatarUrl(req.avatarUrl());
        studentRepo.save(s);
        return toDetailDto(s);
    }

    /**
     * Import hàng loạt hồ sơ sinh viên từ CSV (UTF-8) theo cơ chế Validate & Preview + All-or-Nothing (BR-32).
     * Cột CSV: studentCode,fullName,dateOfBirth(yyyy-MM-dd),gender(MALE/FEMALE/OTHER),nationalId,email,classCode,phone,address
     */
    @Transactional
    public StudentImportResultDto importStudentsCsv(Long actorUserId, MultipartFile file, boolean dryRun) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "IMPORT_EMPTY_FILE", "File tải lên rỗng");
        }

        List<ImportErrorRow> errors = new ArrayList<>();
        List<CreateStudentRequest> validRequests = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();
        Set<String> seenEmails = new HashSet<>();
        Set<String> seenNationalIds = new HashSet<>();

        int totalRows = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowNum = 0;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (rowNum == 1 && (line.startsWith("\uFEFF") || line.toLowerCase().contains("studentcode"))) {
                    continue;
                }
                if (line.isBlank()) continue;
                totalRows++;

                String[] cols = line.split(",", -1);
                if (cols.length < 7) {
                    errors.add(new ImportErrorRow(rowNum, "", "Thiếu cột dữ liệu (yêu cầu tối thiểu 7 cột)"));
                    continue;
                }

                String code = cols[0].trim().toUpperCase();
                String fullName = cols[1].trim();
                String dobStr = cols[2].trim();
                String genderStr = cols[3].trim().toUpperCase();
                String nationalId = cols[4].trim();
                String email = cols[5].trim().toLowerCase();
                String classCode = cols[6].trim().toUpperCase();
                String phone = cols.length > 7 ? cols[7].trim() : null;
                String address = cols.length > 8 ? cols[8].trim() : null;

                if (code.isBlank() || fullName.isBlank() || nationalId.isBlank() || email.isBlank()) {
                    errors.add(new ImportErrorRow(rowNum, code, "Thiếu mã SV, họ tên, CCCD hoặc email"));
                    continue;
                }
                if (!seenCodes.add(code) || studentRepo.existsByStudentCodeIgnoreCase(code) || userRepo.existsByUsernameIgnoreCase(code)) {
                    errors.add(new ImportErrorRow(rowNum, code, "Mã sinh viên bị trùng lặp"));
                    continue;
                }
                if (!seenEmails.add(email) || userRepo.existsByEmailIgnoreCase(email)) {
                    errors.add(new ImportErrorRow(rowNum, code, "Email bị trùng lặp: " + email));
                    continue;
                }
                if (!seenNationalIds.add(nationalId) || studentRepo.existsByNationalIdIgnoreCase(nationalId)) {
                    errors.add(new ImportErrorRow(rowNum, code, "Số CCCD bị trùng lặp: " + nationalId));
                    continue;
                }

                LocalDate dob;
                try {
                    dob = LocalDate.parse(dobStr);
                } catch (Exception ex) {
                    errors.add(new ImportErrorRow(rowNum, code, "Ngày sinh không đúng định dạng yyyy-MM-dd"));
                    continue;
                }

                Student.Gender gender;
                try {
                    gender = Student.Gender.valueOf(genderStr);
                } catch (Exception ex) {
                    errors.add(new ImportErrorRow(rowNum, code, "Giới tính phải là MALE, FEMALE hoặc OTHER"));
                    continue;
                }

                AcademicClass ac = classRepo.findByCodeIgnoreCase(classCode).orElse(null);
                if (ac == null) {
                    errors.add(new ImportErrorRow(rowNum, code, "Không tìm thấy lớp hành chính: " + classCode));
                    continue;
                }

                validRequests.add(new CreateStudentRequest(
                        code, fullName, dob, gender, nationalId, email, null, phone, address,
                        ac.getId(), ac.getMajor().getId(), ac.getCohortYear(), null));
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "IMPORT_READ_ERROR",
                    "Không thể đọc file CSV: " + ex.getMessage());
        }

        if (!errors.isEmpty()) {
            if (!dryRun) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "IMPORT_VALIDATION_FAILED",
                        "File import có " + errors.size() + " dòng lỗi. Đã hủy toàn bộ giao dịch (All-or-Nothing).",
                        errors.stream().map(e -> new com.ptit.training.common.ApiError.FieldIssue(
                                "row-" + e.rowNumber(), e.studentCode() + ": " + e.message())).toList());
            }
            return new StudentImportResultDto(true, totalRows, validRequests.size(), 0, errors);
        }

        if (dryRun) {
            return new StudentImportResultDto(true, totalRows, validRequests.size(), 0, List.of());
        }

        for (CreateStudentRequest req : validRequests) {
            createStudent(actorUserId, req);
        }
        return new StudentImportResultDto(false, totalRows, validRequests.size(), validRequests.size(), List.of());
    }

    private StudentSummaryDto toSummaryDto(Student s) {
        return new StudentSummaryDto(
                s.getId(), s.getUser().getId(), s.getStudentCode(), s.getFullName(),
                s.getDateOfBirth(), s.getGender().name(), s.getUser().getEmail(), s.getPhone(),
                s.getAcademicClass().getId(), s.getAcademicClass().getCode(),
                s.getMajor().getId(), s.getMajor().getCode(), s.getMajor().getName(),
                s.getCohortYear(), s.getAcademicStatus().name()
        );
    }

    private StudentDetailDto toDetailDto(Student s) {
        List<StatusHistoryDto> history = historyRepo.findByStudentIdOrderByChangedAtDesc(s.getId()).stream()
                .map(h -> new StatusHistoryDto(
                        h.getId(),
                        h.getOldStatus() != null ? h.getOldStatus().name() : null,
                        h.getNewStatus().name(),
                        h.getReason(),
                        h.getChangedBy().getUsername(),
                        h.getChangedAt()))
                .toList();

        return new StudentDetailDto(
                s.getId(), s.getUser().getId(), s.getStudentCode(), s.getFullName(),
                s.getDateOfBirth(), s.getGender().name(), s.getNationalId(),
                s.getUser().getEmail(), s.getPersonalEmail(), s.getPhone(), s.getAddress(), s.getAvatarUrl(),
                s.getAcademicClass().getId(), s.getAcademicClass().getCode(), s.getAcademicClass().getName(),
                s.getMajor().getId(), s.getMajor().getCode(), s.getMajor().getName(),
                s.getMajor().getFaculty().getId(), s.getMajor().getFaculty().getName(),
                s.getCohortYear(), s.getAcademicStatus().name(), s.getCreatedAt(), history
        );
    }
}
