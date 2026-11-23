package com.ptit.training.organization.service;

import com.ptit.training.auth.entity.Role;
import com.ptit.training.auth.entity.User;
import com.ptit.training.auth.repository.RoleRepository;
import com.ptit.training.auth.repository.UserRepository;
import com.ptit.training.common.PageResponse;
import com.ptit.training.common.exception.ConflictException;
import com.ptit.training.common.exception.ResourceNotFoundException;
import com.ptit.training.organization.dto.OrganizationDtos.*;
import com.ptit.training.organization.entity.*;
import com.ptit.training.organization.repository.*;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quản lý cơ cấu tổ chức đào tạo (Khoa, Bộ môn, Ngành, Lớp hành chính, Giảng viên).
 */
@Service
public class OrganizationService {

    private final FacultyRepository facultyRepo;
    private final DepartmentRepository departmentRepo;
    private final MajorRepository majorRepo;
    private final AcademicClassRepository classRepo;
    private final LecturerRepository lecturerRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public OrganizationService(
            FacultyRepository facultyRepo,
            DepartmentRepository departmentRepo,
            MajorRepository majorRepo,
            AcademicClassRepository classRepo,
            LecturerRepository lecturerRepo,
            UserRepository userRepo,
            RoleRepository roleRepo,
            PasswordEncoder passwordEncoder) {
        this.facultyRepo = facultyRepo;
        this.departmentRepo = departmentRepo;
        this.majorRepo = majorRepo;
        this.classRepo = classRepo;
        this.lecturerRepo = lecturerRepo;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // --- FACULTY ---
    @Transactional(readOnly = true)
    public List<FacultyDto> listFaculties(Faculty.Status status) {
        List<Faculty> list = status == null
                ? facultyRepo.findAllByOrderByCodeAsc()
                : facultyRepo.findByStatusOrderByCodeAsc(status);
        return list.stream().map(this::toFacultyDto).toList();
    }

    @Transactional
    public FacultyDto createFaculty(CreateFacultyRequest req) {
        String code = req.code().trim().toUpperCase();
        if (facultyRepo.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("FACULTY_CODE_EXISTS", "Mã khoa đã tồn tại: " + code);
        }
        Faculty f = new Faculty();
        f.setCode(code);
        f.setName(req.name().trim());
        f.setDescription(req.description());
        return toFacultyDto(facultyRepo.save(f));
    }

    @Transactional
    public FacultyDto updateFaculty(Long id, UpdateFacultyRequest req) {
        Faculty f = facultyRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FACULTY_NOT_FOUND", "Không tìm thấy khoa"));
        f.setName(req.name().trim());
        f.setDescription(req.description());
        f.setStatus(req.status());
        return toFacultyDto(facultyRepo.save(f));
    }

    // --- DEPARTMENT ---
    @Transactional(readOnly = true)
    public List<DepartmentDto> listDepartments(Long facultyId) {
        List<Department> list = facultyId == null
                ? departmentRepo.findAllByOrderByCodeAsc()
                : departmentRepo.findByFacultyIdOrderByCodeAsc(facultyId);
        return list.stream().map(this::toDepartmentDto).toList();
    }

    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentRequest req) {
        String code = req.code().trim().toUpperCase();
        if (departmentRepo.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("DEPARTMENT_CODE_EXISTS", "Mã bộ môn đã tồn tại: " + code);
        }
        Faculty faculty = facultyRepo.findById(req.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("FACULTY_NOT_FOUND", "Không tìm thấy khoa"));
        Department d = new Department();
        d.setCode(code);
        d.setName(req.name().trim());
        d.setFaculty(faculty);
        return toDepartmentDto(departmentRepo.save(d));
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, UpdateDepartmentRequest req) {
        Department d = departmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DEPARTMENT_NOT_FOUND", "Không tìm thấy bộ môn"));
        Faculty faculty = facultyRepo.findById(req.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("FACULTY_NOT_FOUND", "Không tìm thấy khoa"));
        d.setName(req.name().trim());
        d.setFaculty(faculty);
        d.setStatus(req.status());
        return toDepartmentDto(departmentRepo.save(d));
    }

    // --- MAJOR ---
    @Transactional(readOnly = true)
    public List<MajorDto> listMajors(Long facultyId) {
        List<Major> list = facultyId == null
                ? majorRepo.findAllByOrderByCodeAsc()
                : majorRepo.findByFacultyIdOrderByCodeAsc(facultyId);
        return list.stream().map(this::toMajorDto).toList();
    }

    @Transactional
    public MajorDto createMajor(CreateMajorRequest req) {
        String code = req.code().trim().toUpperCase();
        if (majorRepo.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("MAJOR_CODE_EXISTS", "Mã ngành đã tồn tại: " + code);
        }
        Faculty faculty = facultyRepo.findById(req.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("FACULTY_NOT_FOUND", "Không tìm thấy khoa"));
        Major m = new Major();
        m.setCode(code);
        m.setName(req.name().trim());
        m.setFaculty(faculty);
        m.setTotalCreditsRequired(req.totalCreditsRequired());
        if (req.degreeLevel() != null) {
            m.setDegreeLevel(req.degreeLevel());
        }
        return toMajorDto(majorRepo.save(m));
    }

    @Transactional
    public MajorDto updateMajor(Long id, UpdateMajorRequest req) {
        Major m = majorRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MAJOR_NOT_FOUND", "Không tìm thấy ngành"));
        Faculty faculty = facultyRepo.findById(req.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("FACULTY_NOT_FOUND", "Không tìm thấy khoa"));
        m.setName(req.name().trim());
        m.setFaculty(faculty);
        m.setTotalCreditsRequired(req.totalCreditsRequired());
        if (req.degreeLevel() != null) {
            m.setDegreeLevel(req.degreeLevel());
        }
        m.setStatus(req.status());
        return toMajorDto(majorRepo.save(m));
    }

    // --- ACADEMIC CLASS ---
    @Transactional(readOnly = true)
    public List<AcademicClassDto> listAcademicClasses(Long majorId, Integer cohortYear) {
        return classRepo.search(majorId, cohortYear).stream().map(this::toClassDto).toList();
    }

    @Transactional
    public AcademicClassDto createAcademicClass(CreateAcademicClassRequest req) {
        String code = req.code().trim().toUpperCase();
        if (classRepo.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("ACADEMIC_CLASS_CODE_EXISTS", "Mã lớp hành chính đã tồn tại: " + code);
        }
        Major major = majorRepo.findById(req.majorId())
                .orElseThrow(() -> new ResourceNotFoundException("MAJOR_NOT_FOUND", "Không tìm thấy ngành"));
        AcademicClass ac = new AcademicClass();
        ac.setCode(code);
        ac.setName(req.name().trim());
        ac.setMajor(major);
        ac.setCohortYear(req.cohortYear());
        return toClassDto(classRepo.save(ac));
    }

    @Transactional
    public AcademicClassDto updateAcademicClass(Long id, UpdateAcademicClassRequest req) {
        AcademicClass ac = classRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ACADEMIC_CLASS_NOT_FOUND", "Không tìm thấy lớp hành chính"));
        Major major = majorRepo.findById(req.majorId())
                .orElseThrow(() -> new ResourceNotFoundException("MAJOR_NOT_FOUND", "Không tìm thấy ngành"));
        ac.setName(req.name().trim());
        ac.setMajor(major);
        ac.setCohortYear(req.cohortYear());
        ac.setStatus(req.status());
        return toClassDto(classRepo.save(ac));
    }

    // --- LECTURER ---
    @Transactional(readOnly = true)
    public PageResponse<LecturerDto> searchLecturers(Long departmentId, String keyword, Pageable pageable) {
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Lecturer> page = lecturerRepo.search(departmentId, cleanKeyword, pageable);
        return PageResponse.from(page.map(this::toLecturerDto));
    }

    @Transactional
    public LecturerDto createLecturer(CreateLecturerRequest req) {
        String code = req.lecturerCode().trim().toUpperCase();
        String email = req.email().trim().toLowerCase();

        if (lecturerRepo.existsByLecturerCodeIgnoreCase(code) || userRepo.existsByUsernameIgnoreCase(code)) {
            throw new ConflictException("LECTURER_CODE_EXISTS", "Mã giảng viên đã tồn tại: " + code);
        }
        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("EMAIL_EXISTS", "Email đã tồn tại: " + email);
        }

        Department dept = departmentRepo.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("DEPARTMENT_NOT_FOUND", "Không tìm thấy bộ môn"));

        Role lecturerRole = roleRepo.findByCode("LECTURER")
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_NOT_FOUND", "Thiếu vai trò LECTURER"));

        String rawPassword = (req.initialPassword() != null && !req.initialPassword().isBlank())
                ? req.initialPassword()
                : "Ptit@12345";

        User user = new User();
        user.setUsername(code);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(User.Status.ACTIVE);
        user.setRoles(Set.of(lecturerRole));
        User savedUser = userRepo.save(user);

        Lecturer lecturer = new Lecturer();
        lecturer.setUser(savedUser);
        lecturer.setLecturerCode(code);
        lecturer.setFullName(req.fullName().trim());
        lecturer.setDegree(req.degree() != null && !req.degree().isBlank() ? req.degree().trim() : "ThS");
        lecturer.setAcademicRank(req.academicRank());
        lecturer.setDepartment(dept);
        lecturer.setPhone(req.phone());

        return toLecturerDto(lecturerRepo.save(lecturer));
    }

    @Transactional
    public LecturerDto updateLecturer(Long id, UpdateLecturerRequest req) {
        Lecturer l = lecturerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LECTURER_NOT_FOUND", "Không tìm thấy giảng viên"));
        Department dept = departmentRepo.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("DEPARTMENT_NOT_FOUND", "Không tìm thấy bộ môn"));
        l.setFullName(req.fullName().trim());
        l.setDepartment(dept);
        l.setDegree(req.degree());
        l.setAcademicRank(req.academicRank());
        l.setPhone(req.phone());
        l.setStatus(req.status());
        return toLecturerDto(lecturerRepo.save(l));
    }

    private FacultyDto toFacultyDto(Faculty f) {
        return new FacultyDto(f.getId(), f.getCode(), f.getName(), f.getDescription(), f.getStatus().name());
    }

    private DepartmentDto toDepartmentDto(Department d) {
        return new DepartmentDto(d.getId(), d.getCode(), d.getName(),
                d.getFaculty().getId(), d.getFaculty().getName(), d.getStatus().name());
    }

    private MajorDto toMajorDto(Major m) {
        return new MajorDto(m.getId(), m.getCode(), m.getName(),
                m.getFaculty().getId(), m.getFaculty().getName(), m.getTotalCreditsRequired(),
                m.getDegreeLevel().name(), m.getStatus().name());
    }

    private AcademicClassDto toClassDto(AcademicClass ac) {
        return new AcademicClassDto(ac.getId(), ac.getCode(), ac.getName(),
                ac.getMajor().getId(), ac.getMajor().getCode(), ac.getMajor().getName(),
                ac.getCohortYear(), ac.getStatus().name());
    }

    private LecturerDto toLecturerDto(Lecturer l) {
        return new LecturerDto(
                l.getId(), l.getUser().getId(), l.getLecturerCode(), l.getFullName(), l.getUser().getEmail(),
                l.getDegree(), l.getAcademicRank(), l.getDepartment().getId(), l.getDepartment().getName(),
                l.getDepartment().getFaculty().getId(), l.getDepartment().getFaculty().getName(),
                l.getPhone(), l.getStatus().name()
        );
    }
}
