package com.ptit.training;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.training.auth.entity.User;
import com.ptit.training.auth.repository.UserRepository;
import com.ptit.training.course.entity.Course;
import com.ptit.training.course.repository.CourseRepository;
import com.ptit.training.enrollment.entity.Enrollment;
import com.ptit.training.enrollment.repository.EnrollmentRepository;
import com.ptit.training.enrollment.service.EnrollmentService;
import com.ptit.training.organization.entity.AcademicClass;
import com.ptit.training.organization.entity.Major;
import com.ptit.training.organization.repository.AcademicClassRepository;
import com.ptit.training.organization.repository.MajorRepository;
import com.ptit.training.reporting.repository.AuditLogRepository;
import com.ptit.training.section.entity.Classroom;
import com.ptit.training.section.entity.CourseSection;
import com.ptit.training.section.entity.SectionSchedule;
import com.ptit.training.section.entity.Semester;
import com.ptit.training.section.repository.ClassroomRepository;
import com.ptit.training.section.repository.CourseSectionRepository;
import com.ptit.training.section.repository.SemesterRepository;
import com.ptit.training.student.dto.StudentDtos;
import com.ptit.training.student.entity.Student;
import com.ptit.training.student.repository.StudentRepository;
import com.ptit.training.student.service.StudentService;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAndBusinessIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private StudentService studentService;
    @Autowired private MajorRepository majorRepo;
    @Autowired private AcademicClassRepository classRepo;
    @Autowired private CourseRepository courseRepo;
    @Autowired private SemesterRepository semesterRepo;
    @Autowired private ClassroomRepository classroomRepo;
    @Autowired private CourseSectionRepository sectionRepo;
    @Autowired private EnrollmentRepository enrollmentRepo;
    @Autowired private EnrollmentService enrollmentService;
    @Autowired private AuditLogRepository auditLogRepo;

    private Cookie[] loginAs(String username) throws Exception {
        String body = "{\"usernameOrEmail\":\"" + username + "\",\"password\":\"Ptit@12345\"}";
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return res.getResponse().getCookies();
    }

    @Test
    void tcAuth01_02_04_05_06_07_shouldEnforceAuthenticationRbacAndDataScope() throws Exception {
        // TC-AUTH-02: Unauthenticated request -> 401
        mockMvc.perform(get("/api/v1/students/me"))
                .andExpect(status().isUnauthorized());

        // TC-AUTH-01: Login sets HttpOnly cookies
        Cookie[] sv1Cookies = loginAs("B22DCCN001");
        assertThat(sv1Cookies).extracting(Cookie::getName).contains("access_token", "refresh_token");

        // TC-AUTH-04: Student cannot access grade entry endpoint -> 403
        mockMvc.perform(put("/api/v1/sections/2/grades")
                        .cookie(sv1Cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rows\":[{\"enrollmentId\":2,\"scores\":[{\"gradeComponentId\":5,\"score\":9.0}]}]}"))
                .andExpect(status().isForbidden());

        // TC-AUTH-06: Student cannot view another student's profile -> 403
        mockMvc.perform(get("/api/v1/students/2").cookie(sv1Cookies))
                .andExpect(status().isForbidden());

        // TC-AUTH-07: Student self-contact update ignores privileged fields (BR-09)
        mockMvc.perform(patch("/api/v1/students/me/contact")
                        .cookie(sv1Cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"0999888777\",\"fullName\":\"Hacker Name\",\"academicStatus\":\"GRADUATED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("0999888777"))
                .andExpect(jsonPath("$.data.fullName").value("Phạm Minh Tuấn"))
                .andExpect(jsonPath("$.data.academicStatus").value("ACTIVE"));

        // TC-AUTH-05: Lecturer GV002 cannot view/edit grades of section 2 (assigned to GV001) -> 403
        Cookie[] gv2Cookies = loginAs("GV002");
        mockMvc.perform(get("/api/v1/sections/2/grades").cookie(gv2Cookies))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GRADE_NOT_ASSIGNED_LECTURER"));
    }

    @Test
    void tcAuth03_lockedUserShouldBeBlockedImmediatelyAndRevokeRefreshToken() throws Exception {
        Cookie[] adminCookies = loginAs("admin");
        Cookie[] sv2Cookies = loginAs("B22DCCN002");
        User sv2User = userRepo.findByUsernameOrEmailWithRolesAndPermissions("B22DCCN002").orElseThrow();

        // Admin locks B22DCCN002
        mockMvc.perform(patch("/api/v1/admin/users/" + sv2User.getId() + "/status")
                        .cookie(adminCookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"LOCKED\",\"reason\":\"Test lock account\"}"))
                .andExpect(status().isOk());

        // Refresh token fails
        mockMvc.perform(post("/api/v1/auth/refresh").cookie(sv2Cookies))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_ACCOUNT_LOCKED"));

        // Login fails
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usernameOrEmail\":\"B22DCCN002\",\"password\":\"Ptit@12345\"}"))
                .andExpect(status().isForbidden());

        // Unlock again so other tests are unaffected
        mockMvc.perform(patch("/api/v1/admin/users/" + sv2User.getId() + "/status")
                        .cookie(adminCookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\",\"reason\":\"Restore after test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void tcSec02_shouldRejectOverlappingClassroomSchedule() throws Exception {
        Cookie[] pdtCookies = loginAs("pdt01");
        // Section 2 (INT1306) is at classroom 1 (401-A2), day 2, periods 1-3, weeks 1-15
        // Try opening INT1313 group 2 at same room 1, day 2, periods 3-5, weeks 10-15 -> CONFLICT
        String payload = """
                {
                  "courseId": 3,
                  "semesterId": 2,
                  "groupNumber": 9,
                  "lecturerId": 2,
                  "maxCapacity": 40,
                  "schedules": [
                    {
                      "dayOfWeek": 2,
                      "startPeriod": 3,
                      "endPeriod": 5,
                      "startWeek": 10,
                      "endWeek": 15,
                      "classroomId": 1
                    }
                  ]
                }
                """;
        mockMvc.perform(post("/api/v1/sections")
                        .cookie(pdtCookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("SCHEDULE_CLASSROOM_CONFLICT"));
    }

    @Test
    void tcEnr02_shouldBlockStudentWithoutPrerequisite() throws Exception {
        // B22DCCN002 has NOT passed INT1154, try enrolling into INT1306 (section 2)
        Cookie[] sv2Cookies = loginAs("B22DCCN002");
        mockMvc.perform(post("/api/v1/enrollments")
                        .cookie(sv2Cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectionId\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ENROLL_PREREQUISITE_NOT_MET"));
    }

    @Test
    void tcEnr01_10_shouldEnrollAndCancelAndReEnrollCleanly() throws Exception {
        Cookie[] sv1Cookies = loginAs("B22DCCN001");
        // Section 3 is INT1313-20261-01 (no prerequisite, day 4 periods 7-9, no conflict for SV1)
        MvcResult enrollRes = mockMvc.perform(post("/api/v1/enrollments")
                        .cookie(sv1Cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectionId\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ENROLLED"))
                .andReturn();

        JsonNode root = objectMapper.readTree(enrollRes.getResponse().getContentAsString());
        long enrollmentId = root.get("data").get("id").asLong();

        // Cancel
        mockMvc.perform(delete("/api/v1/enrollments/" + enrollmentId).cookie(sv1Cookies))
                .andExpect(status().isOk());

        Enrollment cancelled = enrollmentRepo.findById(enrollmentId).orElseThrow();
        assertThat(cancelled.getStatus()).isEqualTo(Enrollment.Status.CANCELLED);

        // Re-enroll
        mockMvc.perform(post("/api/v1/enrollments")
                        .cookie(sv1Cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectionId\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ENROLLED"));

        // Clean up back to cancelled
        mockMvc.perform(delete("/api/v1/enrollments/" + enrollmentId).cookie(sv1Cookies))
                .andExpect(status().isOk());
    }

    @Test
    void tcEnr09_concurrencyTest_onlyOneStudentWinsLastSeat() throws Exception {
        // Tạo 1 lớp học phần riêng có sức chứa đúng 1 chỗ (maxCapacity = 1, currentEnrolled = 0)
        Course course = courseRepo.findAll().stream()
                .filter(c -> c.getCode().equals("INT1313")).findFirst().orElseThrow();
        Semester sem = semesterRepo.findAll().stream()
                .filter(s -> s.getCode().equals("20261")).findFirst().orElseThrow();
        Classroom room = classroomRepo.findAll().get(0);

        CourseSection raceSection = new CourseSection();
        raceSection.setSectionCode("INT1313-20261-99");
        raceSection.setCourse(course);
        raceSection.setSemester(sem);
        raceSection.setGroupNumber(99);
        raceSection.setMaxCapacity(1);
        raceSection.setCurrentEnrolled(0);
        raceSection.setStatus(CourseSection.Status.OPEN);

        SectionSchedule sch = new SectionSchedule();
        sch.setSection(raceSection);
        sch.setDayOfWeek(7);
        sch.setStartPeriod(10);
        sch.setEndPeriod(12);
        sch.setStartWeek(1);
        sch.setEndWeek(15);
        sch.setClassroom(room);
        raceSection.getSchedules().add(sch);
        CourseSection savedSection = sectionRepo.save(raceSection);

        // Tạo 10 sinh viên hợp lệ
        Major major = majorRepo.findAll().get(0);
        AcademicClass ac = classRepo.findAll().get(0);
        User admin = userRepo.findByUsernameOrEmailWithRolesAndPermissions("admin").orElseThrow();

        List<Long> testStudentUserIds = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String code = String.format("B22RACE%03d", i);
            StudentDtos.StudentDetailDto created = studentService.createStudent(
                    admin.getId(),
                    new StudentDtos.CreateStudentRequest(
                            code, "Race Student " + i, LocalDate.of(2004, 1, 1),
                            Student.Gender.MALE, String.format("09990000%04d", i),
                            code.toLowerCase() + "@stu.ptit-demo.vn", null, null, null,
                            ac.getId(), major.getId(), 2022, "Ptit@12345"
                    ));
            testStudentUserIds.add(created.userId());
        }

        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger fullFailCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (Long userId : testStudentUserIds) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    enrollmentService.enrollStudent(userId, savedSection.getId());
                    successCount.incrementAndGet();
                } catch (Exception ex) {
                    fullFailCount.incrementAndGet();
                }
            }));
        }

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        pool.shutdown();

        CourseSection reloaded = sectionRepo.findById(savedSection.getId()).orElseThrow();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(fullFailCount.get()).isEqualTo(9);
        assertThat(reloaded.getCurrentEnrolled()).isEqualTo(1);
    }

    @Test
    void tcStu02_importStudentsCsvAllOrNothingShouldRollbackOnSingleError() throws Exception {
        Cookie[] pdtCookies = loginAs("pdt01");
        long beforeCount = studentRepo.count();

        String csvContent = """
                studentCode,fullName,dateOfBirth,gender,nationalId,email,classCode,phone,address
                B22IMP001,Nguyen Van Mot,2004-03-15,MALE,077204000101,b22imp001@stu.ptit-demo.vn,D22CQCN01-B,0911111111,Ha Noi
                B22IMP002,Tran Thi Hai,2004-04-20,FEMALE,001204000001,b22imp002@stu.ptit-demo.vn,D22CQCN01-B,0911111112,Ha Noi
                """;
        // Row 2 duplicates nationalId 001204000001 of B22DCCN001!
        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/api/v1/students/import")
                        .file(file)
                        .param("dryRun", "false")
                        .cookie(pdtCookies))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("IMPORT_VALIDATION_FAILED"));

        // All-or-Nothing: row 1 B22IMP001 must NOT be saved
        assertThat(studentRepo.count()).isEqualTo(beforeCount);
    }

    @Test
    void tcGrd04_05_06_gradeWorkflowPublishLockAndOverrideAudit() throws Exception {
        Cookie[] gv1Cookies = loginAs("GV001");
        Cookie[] pdtCookies = loginAs("pdt01");

        // GV001 gets gradebook of section 2 (INT1306-20261-01, has enrollment id 2)
        MvcResult gbRes = mockMvc.perform(get("/api/v1/sections/2/grades").cookie(gv1Cookies))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode gb = objectMapper.readTree(gbRes.getResponse().getContentAsString()).get("data");
        long ccId = gb.get("components").get(0).get("id").asLong();
        long thId = gb.get("components").get(1).get("id").asLong();
        long gkId = gb.get("components").get(2).get("id").asLong();
        long ckId = gb.get("components").get(3).get("id").asLong();
        long enrId = gb.get("rows").get(0).get("enrollmentId").asLong();

        // Save DRAFT grades
        String saveBody = String.format("""
                {
                  "rows": [
                    {
                      "enrollmentId": %d,
                      "scores": [
                        {"gradeComponentId": %d, "score": 9.0},
                        {"gradeComponentId": %d, "score": 8.0},
                        {"gradeComponentId": %d, "score": 8.5},
                        {"gradeComponentId": %d, "score": 8.5}
                      ]
                    }
                  ]
                }
                """, enrId, ccId, thId, gkId, ckId);

        mockMvc.perform(put("/api/v1/sections/2/grades")
                        .cookie(gv1Cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saveBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rows[0].finalScore10").value(8.5))
                .andExpect(jsonPath("$.data.rows[0].letterGrade").value("A"));

        // TC-GRD-04: Student still cannot see DRAFT score
        Cookie[] sv1Cookies = loginAs("B22DCCN001");
        mockMvc.perform(get("/api/v1/students/me/academic-results").cookie(sv1Cookies))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.semesters[1].courses[0].gradeStatus").value("DRAFT"))
                .andExpect(jsonPath("$.data.semesters[1].courses[0].finalScore10").doesNotExist());

        // Publish grades -> PUBLISHED
        mockMvc.perform(post("/api/v1/sections/2/grades/publish").cookie(gv1Cookies))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gradeStatus").value("PUBLISHED"));

        // TC-GRD-05: Lecturer blocked from editing after PUBLISHED
        mockMvc.perform(put("/api/v1/sections/2/grades")
                        .cookie(gv1Cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saveBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("GRADE_ALREADY_PUBLISHED_OR_LOCKED"));

        // PĐT locks grades -> LOCKED
        mockMvc.perform(post("/api/v1/sections/2/grades/lock").cookie(pdtCookies))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gradeStatus").value("LOCKED"));

        // TC-GRD-06: PĐT overrides score after lock with reason -> AuditLog recorded
        long auditBefore = auditLogRepo.count();
        String overrideBody = String.format("""
                {
                  "scores": [{"gradeComponentId": %d, "score": 9.5}],
                  "reason": "Điều chỉnh theo biên bản phúc khảo số 12/PK"
                }
                """, ckId);
        mockMvc.perform(patch("/api/v1/enrollments/" + enrId + "/grades/override")
                        .cookie(pdtCookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(overrideBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalScore10").value(9.0));

        assertThat(auditLogRepo.count()).isGreaterThan(auditBefore);
    }
}
