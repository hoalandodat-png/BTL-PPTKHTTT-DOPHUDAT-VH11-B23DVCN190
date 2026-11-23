package com.ptit.training.common.security;

import com.ptit.training.auth.entity.Permission;
import com.ptit.training.auth.entity.Role;
import com.ptit.training.auth.entity.User;
import com.ptit.training.auth.repository.PermissionRepository;
import com.ptit.training.auth.repository.RoleRepository;
import com.ptit.training.auth.repository.UserRepository;
import com.ptit.training.course.entity.Course;
import com.ptit.training.course.entity.CoursePrerequisite;
import com.ptit.training.course.entity.Curriculum;
import com.ptit.training.course.entity.CurriculumCourse;
import com.ptit.training.course.entity.GradeComponent;
import com.ptit.training.course.repository.CoursePrerequisiteRepository;
import com.ptit.training.course.repository.CourseRepository;
import com.ptit.training.course.repository.CurriculumRepository;
import com.ptit.training.enrollment.entity.Enrollment;
import com.ptit.training.enrollment.repository.EnrollmentRepository;
import com.ptit.training.grade.entity.GradingScale;
import com.ptit.training.grade.entity.SystemConfig;
import com.ptit.training.grade.repository.GradingScaleRepository;
import com.ptit.training.grade.repository.SystemConfigRepository;
import com.ptit.training.organization.entity.AcademicClass;
import com.ptit.training.organization.entity.Department;
import com.ptit.training.organization.entity.Faculty;
import com.ptit.training.organization.entity.Lecturer;
import com.ptit.training.organization.entity.Major;
import com.ptit.training.organization.repository.AcademicClassRepository;
import com.ptit.training.organization.repository.DepartmentRepository;
import com.ptit.training.organization.repository.FacultyRepository;
import com.ptit.training.organization.repository.LecturerRepository;
import com.ptit.training.organization.repository.MajorRepository;
import com.ptit.training.section.entity.AcademicYear;
import com.ptit.training.section.entity.Classroom;
import com.ptit.training.section.entity.CourseSection;
import com.ptit.training.section.entity.SectionSchedule;
import com.ptit.training.section.entity.Semester;
import com.ptit.training.section.repository.AcademicYearRepository;
import com.ptit.training.section.repository.ClassroomRepository;
import com.ptit.training.section.repository.CourseSectionRepository;
import com.ptit.training.section.repository.SemesterRepository;
import com.ptit.training.student.entity.Student;
import com.ptit.training.student.entity.StudentStatusHistory;
import com.ptit.training.student.repository.StudentRepository;
import com.ptit.training.student.repository.StudentStatusHistoryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Đảm bảo dữ liệu demo giả lập PTIT (mật khẩu "Ptit@12345") luôn đồng bộ trong cả PostgreSQL (Flyway) và H2 Test.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PermissionRepository permRepo;
    private final GradingScaleRepository scaleRepo;
    private final SystemConfigRepository configRepo;
    private final FacultyRepository facultyRepo;
    private final DepartmentRepository deptRepo;
    private final MajorRepository majorRepo;
    private final AcademicClassRepository classRepo;
    private final LecturerRepository lecturerRepo;
    private final StudentRepository studentRepo;
    private final StudentStatusHistoryRepository historyRepo;
    private final CourseRepository courseRepo;
    private final CoursePrerequisiteRepository prereqRepo;
    private final CurriculumRepository curriculumRepo;
    private final AcademicYearRepository yearRepo;
    private final SemesterRepository semesterRepo;
    private final ClassroomRepository classroomRepo;
    private final CourseSectionRepository sectionRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepo, RoleRepository roleRepo, PermissionRepository permRepo,
            GradingScaleRepository scaleRepo, SystemConfigRepository configRepo,
            FacultyRepository facultyRepo, DepartmentRepository deptRepo, MajorRepository majorRepo,
            AcademicClassRepository classRepo, LecturerRepository lecturerRepo,
            StudentRepository studentRepo, StudentStatusHistoryRepository historyRepo,
            CourseRepository courseRepo, CoursePrerequisiteRepository prereqRepo,
            CurriculumRepository curriculumRepo, AcademicYearRepository yearRepo,
            SemesterRepository semesterRepo, ClassroomRepository classroomRepo,
            CourseSectionRepository sectionRepo, EnrollmentRepository enrollmentRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.permRepo = permRepo;
        this.scaleRepo = scaleRepo;
        this.configRepo = configRepo;
        this.facultyRepo = facultyRepo;
        this.deptRepo = deptRepo;
        this.majorRepo = majorRepo;
        this.classRepo = classRepo;
        this.lecturerRepo = lecturerRepo;
        this.studentRepo = studentRepo;
        this.historyRepo = historyRepo;
        this.courseRepo = courseRepo;
        this.prereqRepo = prereqRepo;
        this.curriculumRepo = curriculumRepo;
        this.yearRepo = yearRepo;
        this.semesterRepo = semesterRepo;
        this.classroomRepo = classroomRepo;
        this.sectionRepo = sectionRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Nếu Flyway đã seed dữ liệu rồi, chỉ đảm bảo mật khẩu demo "Ptit@12345" khớp chuẩn với BCryptPasswordEncoder
        if (roleRepo.count() > 0) {
            ensureDemoPasswordHashes();
            return;
        }
        seedAllForFreshDatabase();
    }

    private void ensureDemoPasswordHashes() {
        List<String> demoUsers = List.of("admin", "pdt01", "GV001", "GV002", "B22DCCN001", "B22DCCN002");
        for (String uname : demoUsers) {
            userRepo.findByUsernameOrEmailWithRolesAndPermissions(uname).ifPresent(u -> {
                if (!passwordEncoder.matches("Ptit@12345", u.getPasswordHash())) {
                    u.setPasswordHash(passwordEncoder.encode("Ptit@12345"));
                    userRepo.save(u);
                }
            });
        }
    }

    private void seedAllForFreshDatabase() {
        Map<String, Permission> p = new LinkedHashMap<>();
        String[][] permDefs = {
                {"USER_READ", "Xem tài khoản", "AUTH"},
                {"USER_WRITE", "Tạo và cập nhật tài khoản", "AUTH"},
                {"USER_LOCK", "Khóa và mở khóa tài khoản", "AUTH"},
                {"RBAC_MANAGE", "Quản lý vai trò và quyền hạn", "AUTH"},
                {"ORG_READ", "Xem danh mục Khoa/Bộ môn/Ngành/Lớp", "ORG"},
                {"ORG_WRITE", "Quản lý Khoa/Bộ môn/Ngành/Lớp/GV", "ORG"},
                {"STUDENT_READ", "Xem hồ sơ sinh viên", "STUDENT"},
                {"STUDENT_WRITE", "Tạo, sửa và import hồ sơ sinh viên", "STUDENT"},
                {"STUDENT_SELF_UPDATE", "Sinh viên tự sửa thông tin liên lạc", "STUDENT"},
                {"COURSE_READ", "Xem học phần và chương trình đào tạo", "COURSE"},
                {"COURSE_WRITE", "Quản lý học phần, tiên quyết, CTĐT", "COURSE"},
                {"SEMESTER_WRITE", "Quản lý học kỳ và đợt đăng ký", "SECTION"},
                {"SECTION_READ", "Tra cứu lớp học phần và lịch học", "SECTION"},
                {"SECTION_WRITE", "Mở lớp học phần và xếp lịch học", "SECTION"},
                {"ENROLLMENT_SELF", "Sinh viên tự đăng ký và hủy học phần", "ENROLLMENT"},
                {"ENROLLMENT_OVERRIDE", "Phòng đào tạo đăng ký/hủy hộ", "ENROLLMENT"},
                {"GRADE_WRITE", "Giảng viên nhập điểm lớp phụ trách", "GRADE"},
                {"GRADE_PUBLISH", "Công bố điểm lớp học phần", "GRADE"},
                {"GRADE_LOCK", "Khóa sổ điểm và sửa điểm sau khóa", "GRADE"},
                {"RESULT_READ_SELF", "Sinh viên xem kết quả học tập", "GRADE"},
                {"REPORT_READ", "Xem báo cáo thống kê đào tạo", "REPORT"},
                {"AUDIT_READ", "Tra cứu nhật ký kiểm toán", "SYSTEM"}
        };
        for (String[] def : permDefs) {
            Permission perm = new Permission();
            perm.setCode(def[0]);
            perm.setName(def[1]);
            perm.setModule(def[2]);
            p.put(def[0], permRepo.save(perm));
        }

        Role rAdmin = createRole("ADMIN", "Quản trị hệ thống",
                List.of(p.get("USER_READ"), p.get("USER_WRITE"), p.get("USER_LOCK"),
                        p.get("RBAC_MANAGE"), p.get("ORG_READ"), p.get("REPORT_READ"), p.get("AUDIT_READ")));
        Role rAA = createRole("ACADEMIC_AFFAIRS", "Phòng Quản lý Đào tạo",
                List.of(p.get("USER_READ"), p.get("ORG_READ"), p.get("ORG_WRITE"),
                        p.get("STUDENT_READ"), p.get("STUDENT_WRITE"), p.get("COURSE_READ"),
                        p.get("COURSE_WRITE"), p.get("SEMESTER_WRITE"), p.get("SECTION_READ"),
                        p.get("SECTION_WRITE"), p.get("ENROLLMENT_OVERRIDE"), p.get("GRADE_PUBLISH"),
                        p.get("GRADE_LOCK"), p.get("REPORT_READ")));
        Role rLec = createRole("LECTURER", "Giảng viên",
                List.of(p.get("ORG_READ"), p.get("STUDENT_READ"), p.get("STUDENT_WRITE"), p.get("COURSE_READ"),
                        p.get("SECTION_READ"), p.get("GRADE_WRITE"), p.get("GRADE_PUBLISH"), p.get("REPORT_READ")));
        Role rStu = createRole("STUDENT", "Sinh viên",
                List.of(p.get("ORG_READ"), p.get("STUDENT_SELF_UPDATE"), p.get("COURSE_READ"),
                        p.get("SECTION_READ"), p.get("ENROLLMENT_SELF"), p.get("RESULT_READ_SELF")));

        seedScale("8.5", "10.0", "A", "4.00", true, 1);
        seedScale("8.0", "8.4", "B+", "3.50", true, 2);
        seedScale("7.0", "7.9", "B", "3.00", true, 3);
        seedScale("6.5", "6.9", "C+", "2.50", true, 4);
        seedScale("5.5", "6.4", "C", "2.00", true, 5);
        seedScale("5.0", "5.4", "D+", "1.50", true, 6);
        seedScale("4.0", "4.9", "D", "1.00", true, 7);
        seedScale("0.0", "3.9", "F", "0.00", false, 8);

        SystemConfig cfg1 = new SystemConfig();
        cfg1.setConfigKey("RETAKE_GPA_POLICY");
        cfg1.setConfigValue("HIGHEST_SCORE");
        cfg1.setValueType("STRING");
        cfg1.setDescription("Lấy điểm cao nhất khi học lại/cải thiện");
        configRepo.save(cfg1);

        String hash = passwordEncoder.encode("Ptit@12345");
        User uAdmin = createUser("admin", "admin@ptit-demo.edu.vn", hash, rAdmin);
        User uPdt = createUser("pdt01", "daotao01@ptit-demo.edu.vn", hash, rAA);
        User uGv1 = createUser("GV001", "gv.huy@ptit-demo.edu.vn", hash, rLec);
        User uGv2 = createUser("GV002", "gv.lan@ptit-demo.edu.vn", hash, rLec);
        User uSv1 = createUser("B22DCCN001", "b22dccn001@stu.ptit-demo.vn", hash, rStu);
        User uSv2 = createUser("B22DCCN002", "b22dccn002@stu.ptit-demo.vn", hash, rStu);

        Faculty fCntt = new Faculty();
        fCntt.setCode("CNTT1");
        fCntt.setName("Khoa Công nghệ thông tin 1");
        facultyRepo.save(fCntt);

        Department dCnpm = new Department();
        dCnpm.setCode("CNPM");
        dCnpm.setName("Bộ môn Công nghệ phần mềm");
        dCnpm.setFaculty(fCntt);
        deptRepo.save(dCnpm);

        Department dHttt = new Department();
        dHttt.setCode("HTTT");
        dHttt.setName("Bộ môn Hệ thống thông tin");
        dHttt.setFaculty(fCntt);
        deptRepo.save(dHttt);

        Major mDccn = new Major();
        mDccn.setCode("DCCN");
        mDccn.setName("Công nghệ thông tin");
        mDccn.setFaculty(fCntt);
        mDccn.setTotalCreditsRequired(150);
        majorRepo.save(mDccn);

        AcademicClass ac1 = new AcademicClass();
        ac1.setCode("D22CQCN01-B");
        ac1.setName("Lớp CNTT 01 - Khóa D22");
        ac1.setMajor(mDccn);
        ac1.setCohortYear(2022);
        classRepo.save(ac1);

        Lecturer lec1 = createLecturer(uGv1, "GV001", "TS. Nguyễn Quang Huy", "TS", "PGS", dCnpm);
        Lecturer lec2 = createLecturer(uGv2, "GV002", "ThS. Trần Thị Mai Lan", "ThS", null, dHttt);

        Student sv1 = createStudent(uSv1, uPdt, "B22DCCN001", "Phạm Minh Tuấn", "001204000001", ac1, mDccn);
        Student sv2 = createStudent(uSv2, uPdt, "B22DCCN002", "Lê Thu Hà", "001204000002", ac1, mDccn);

        Course c1 = createCourse("INT1154", "Tin học cơ sở 2 (Lập trình C)", 3, dCnpm);
        Course c2 = createCourse("INT1306", "Cấu trúc dữ liệu và Giải thuật", 3, dCnpm);
        Course c3 = createCourse("INT1313", "Cơ sở dữ liệu", 3, dHttt);
        Course c4 = createCourse("INT1332", "Lập trình hướng đối tượng (Java)", 3, dCnpm);

        CoursePrerequisite cp1 = new CoursePrerequisite();
        cp1.setCourse(c2);
        cp1.setPrerequisiteCourse(c1);
        cp1.setConditionType(CoursePrerequisite.ConditionType.PREREQUISITE);
        prereqRepo.save(cp1);

        CoursePrerequisite cp2 = new CoursePrerequisite();
        cp2.setCourse(c4);
        cp2.setPrerequisiteCourse(c1);
        cp2.setConditionType(CoursePrerequisite.ConditionType.PREREQUISITE);
        prereqRepo.save(cp2);

        Curriculum curr = new Curriculum();
        curr.setCode("CURR-DCCN-2022");
        curr.setName("Chương trình đào tạo CNTT Khóa 2022");
        curr.setMajor(mDccn);
        curr.setCohortYear(2022);
        curr.setTotalCredits(150);
        addCurrCourse(curr, c1, 2);
        addCurrCourse(curr, c2, 3);
        addCurrCourse(curr, c3, 4);
        addCurrCourse(curr, c4, 4);
        curriculumRepo.save(curr);

        AcademicYear ayPrev = new AcademicYear();
        ayPrev.setCode("2025-2026");
        ayPrev.setName("Năm học 2025 - 2026");
        ayPrev.setStartYear(2025);
        ayPrev.setEndYear(2026);
        ayPrev.setStatus(AcademicYear.Status.ARCHIVED);
        yearRepo.save(ayPrev);

        AcademicYear ayCurr = new AcademicYear();
        ayCurr.setCode("2026-2027");
        ayCurr.setName("Năm học 2026 - 2027");
        ayCurr.setStartYear(2026);
        ayCurr.setEndYear(2027);
        yearRepo.save(ayCurr);

        Instant now = Instant.now();
        Semester semPrev = new Semester();
        semPrev.setCode("20252");
        semPrev.setName("Học kỳ 2 Năm học 2025-2026");
        semPrev.setAcademicYear(ayPrev);
        semPrev.setStartDate(LocalDate.of(2026, 1, 15));
        semPrev.setEndDate(LocalDate.of(2026, 5, 30));
        semPrev.setMinCredits(15);
        semPrev.setMaxCredits(25);
        semPrev.setRegistrationStartAt(now.minus(180, ChronoUnit.DAYS));
        semPrev.setRegistrationEndAt(now.minus(160, ChronoUnit.DAYS));
        semPrev.setIsRegistrationOpen(false);
        semPrev.setStatus(Semester.Status.CLOSED);
        semesterRepo.save(semPrev);

        Semester semCurr = new Semester();
        semCurr.setCode("20261");
        semCurr.setName("Học kỳ 1 Năm học 2026-2027");
        semCurr.setAcademicYear(ayCurr);
        semCurr.setStartDate(LocalDate.of(2026, 9, 1));
        semCurr.setEndDate(LocalDate.of(2027, 1, 15));
        semCurr.setMinCredits(15);
        semCurr.setMaxCredits(25);
        semCurr.setRegistrationStartAt(now.minus(7, ChronoUnit.DAYS));
        semCurr.setRegistrationEndAt(now.plus(30, ChronoUnit.DAYS));
        semCurr.setIsRegistrationOpen(true);
        semCurr.setStatus(Semester.Status.ONGOING);
        semesterRepo.save(semCurr);

        Classroom r1 = createRoom("401-A2", "A2", 60, Classroom.RoomType.THEORY);
        Classroom r2 = createRoom("402-A2", "A2", 60, Classroom.RoomType.THEORY);
        Classroom r3 = createRoom("LAB-301-A3", "A3", 40, Classroom.RoomType.LAB);

        CourseSection s1 = createSection("INT1154-20252-01", c1, semPrev, 1, lec1, 60, 1,
                CourseSection.GradeBookStatus.LOCKED, CourseSection.Status.COMPLETED, 2, 1, 3, r1);
        CourseSection s2 = createSection("INT1306-20261-01", c2, semCurr, 1, lec1, 50, 1,
                CourseSection.GradeBookStatus.DRAFT, CourseSection.Status.OPEN, 2, 1, 3, r1);
        createSection("INT1313-20261-01", c3, semCurr, 1, lec2, 50, 0,
                CourseSection.GradeBookStatus.DRAFT, CourseSection.Status.OPEN, 4, 7, 9, r2);
        createSection("INT1332-20261-01", c4, semCurr, 1, lec1, 40, 0,
                CourseSection.GradeBookStatus.DRAFT, CourseSection.Status.OPEN, 6, 1, 3, r3);

        // SV1 đã qua môn INT1154 ở kỳ trước với điểm A (8.5 / 4.0)
        Enrollment e1 = new Enrollment();
        e1.setStudent(sv1);
        e1.setSection(s1);
        e1.setCourse(c1);
        e1.setSemester(semPrev);
        e1.setStatus(Enrollment.Status.ENROLLED);
        e1.setEnrolledBy(uSv1);
        e1.setFinalScore10(new BigDecimal("8.5"));
        e1.setLetterGrade("A");
        e1.setGpaScore4(new BigDecimal("4.00"));
        e1.setIsPassed(true);
        enrollmentRepo.save(e1);

        // SV1 đang đăng ký INT1306 ở kỳ hiện tại
        Enrollment e2 = new Enrollment();
        e2.setStudent(sv1);
        e2.setSection(s2);
        e2.setCourse(c2);
        e2.setSemester(semCurr);
        e2.setStatus(Enrollment.Status.ENROLLED);
        e2.setEnrolledBy(uSv1);
        enrollmentRepo.save(e2);
    }

    private Role createRole(String code, String name, List<Permission> perms) {
        Role r = new Role();
        r.setCode(code);
        r.setName(name);
        r.setPermissions(new HashSet<>(perms));
        return roleRepo.save(r);
    }

    private void seedScale(String min, String max, String letter, String gpa4, boolean pass, int order) {
        GradingScale gs = new GradingScale();
        gs.setMinScore10(new BigDecimal(min));
        gs.setMaxScore10(new BigDecimal(max));
        gs.setLetterGrade(letter);
        gs.setGpaScore4(new BigDecimal(gpa4));
        gs.setIsPass(pass);
        gs.setDisplayOrder(order);
        scaleRepo.save(gs);
    }

    private User createUser(String username, String email, String hash, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(hash);
        u.setStatus(User.Status.ACTIVE);
        u.setRoles(Set.of(role));
        return userRepo.save(u);
    }

    private Lecturer createLecturer(User user, String code, String name, String degree, String rank, Department dept) {
        Lecturer l = new Lecturer();
        l.setUser(user);
        l.setLecturerCode(code);
        l.setFullName(name);
        l.setDegree(degree);
        l.setAcademicRank(rank);
        l.setDepartment(dept);
        return lecturerRepo.save(l);
    }

    private Student createStudent(User user, User creator, String code, String name, String nid, AcademicClass ac, Major major) {
        Student s = new Student();
        s.setUser(user);
        s.setStudentCode(code);
        s.setFullName(name);
        s.setDateOfBirth(LocalDate.of(2004, 5, 12));
        s.setGender(Student.Gender.MALE);
        s.setNationalId(nid);
        s.setAcademicClass(ac);
        s.setMajor(major);
        s.setCohortYear(2022);
        s.setAcademicStatus(Student.AcademicStatus.ACTIVE);
        Student saved = studentRepo.save(s);

        StudentStatusHistory h = new StudentStatusHistory();
        h.setStudent(saved);
        h.setNewStatus(Student.AcademicStatus.ACTIVE);
        h.setReason("Khởi tạo hồ sơ demo");
        h.setChangedBy(creator);
        historyRepo.save(h);
        return saved;
    }

    private Course createCourse(String code, String name, int credits, Department dept) {
        Course c = new Course();
        c.setCode(code);
        c.setName(name);
        c.setCredits(credits);
        c.setTheoryHours(30);
        c.setPracticeHours(15);
        c.setDepartment(dept);

        c.getGradeComponents().add(comp(c, "ATTENDANCE", "Chuyên cần", 10, false, "0.0", 1));
        c.getGradeComponents().add(comp(c, "PRACTICE", "Kiểm tra / Thực hành", 20, false, "0.0", 2));
        c.getGradeComponents().add(comp(c, "MIDTERM", "Giữa kỳ", 20, false, "0.0", 3));
        c.getGradeComponents().add(comp(c, "FINAL", "Cuối kỳ", 50, true, "4.0", 4));
        return courseRepo.save(c);
    }

    private GradeComponent comp(Course c, String code, String name, int weight, boolean isFinal, String threshold, int order) {
        GradeComponent gc = new GradeComponent();
        gc.setCourse(c);
        gc.setCode(code);
        gc.setName(name);
        gc.setWeightPercent(weight);
        gc.setIsFinalExam(isFinal);
        gc.setMinThreshold(new BigDecimal(threshold));
        gc.setDisplayOrder(order);
        return gc;
    }

    private void addCurrCourse(Curriculum curr, Course c, int sem) {
        CurriculumCourse cc = new CurriculumCourse();
        cc.setCurriculum(curr);
        cc.setCourse(c);
        cc.setRecommendedSemester(sem);
        cc.setIsRequired(true);
        curr.getCurriculumCourses().add(cc);
    }

    private Classroom createRoom(String code, String building, int capacity, Classroom.RoomType type) {
        Classroom r = new Classroom();
        r.setCode(code);
        r.setBuilding(building);
        r.setCapacity(capacity);
        r.setRoomType(type);
        return classroomRepo.save(r);
    }

    private CourseSection createSection(
            String sectionCode, Course course, Semester sem, int group, Lecturer lec,
            int maxCap, int enrolled, CourseSection.GradeBookStatus gradeStatus,
            CourseSection.Status status, int dow, int pStart, int pEnd, Classroom room) {
        CourseSection cs = new CourseSection();
        cs.setSectionCode(sectionCode);
        cs.setCourse(course);
        cs.setSemester(sem);
        cs.setGroupNumber(group);
        cs.setLecturer(lec);
        cs.setMaxCapacity(maxCap);
        cs.setCurrentEnrolled(enrolled);
        cs.setGradeStatus(gradeStatus);
        cs.setStatus(status);

        SectionSchedule sch = new SectionSchedule();
        sch.setSection(cs);
        sch.setDayOfWeek(dow);
        sch.setStartPeriod(pStart);
        sch.setEndPeriod(pEnd);
        sch.setStartWeek(1);
        sch.setEndWeek(15);
        sch.setClassroom(room);
        cs.getSchedules().add(sch);
        return sectionRepo.save(cs);
    }
}
