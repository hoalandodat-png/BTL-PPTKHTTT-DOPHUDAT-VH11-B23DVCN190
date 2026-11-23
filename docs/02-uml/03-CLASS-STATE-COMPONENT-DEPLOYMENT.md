# GIAI ĐOẠN 3 (PHẦN 3): CLASS, STATE, COMPONENT & DEPLOYMENT DIAGRAMS

> [!NOTE]
> Tài liệu này hoàn thiện mục **6.6 (Class Diagram)**, **6.7 (State Diagram)**, **6.8 (Component Diagram)** và **6.9 (Deployment Diagram)** của [Planning.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/Planning.md#L382-L429), kèm **Checklist nghiệm thu Giai đoạn 3**.

---

## 1. CLASS DIAGRAM (MỤC 6.6)

Theo yêu cầu mục 6.6, phần này phân biệt rõ **2 mức độ Class Diagram**:
1. **Domain Analysis Class Diagram (Mức phân tích nghiệp vụ):** Tập trung vào các lớp thực thể miền nghiệp vụ (Domain Entities), thuộc tính, hành vi nghiệp vụ cốt lõi và mối quan hệ giữa chúng (`Association`, `Aggregation o--`, `Composition *--`, `Inheritance <|--`, `Multiplicity`).
2. **Software Design Class Diagram (Mức thiết kế phần mềm – Spring Boot):** Thể hiện cấu trúc phân tầng `Controller → Service → Repository → Entity / DTO` và `Security`.

### 1.1. Giải thích ký hiệu UML Class Diagram sử dụng
- **Visibility (Phạm vi truy cập):** `-` (`private`), `#` (`protected`), `+` (`public`).
- **Inheritance (`<|--`):** Kế thừa (ví dụ `BaseAuditableEntity <|-- CourseSection`).
- **Composition (`*--`):** Quan hệ hợp thành chặt (vòng đời con phụ thuộc hoàn toàn vào cha; ví dụ `Course *-- GradeComponent`, `CourseSection *-- SectionSchedule`, `Enrollment *-- StudentGrade`).
- **Aggregation (`o--`):** Quan hệ tập hợp lỏng (con có thể tồn tại độc lập với cha; ví dụ `Faculty o-- Department`, `Department o-- Lecturer`, `AcademicClass o-- Student`).
- **Association (`-->`):** Liên kết tham chiếu thông thường kèm bản số (`1`, `0..1`, `1..*`, `0..*`).

---

### 1.2. Domain Analysis Class Diagram (Mức phân tích nghiệp vụ)

```mermaid
classDiagram
    direction TB

    class User {
        -Long id
        -String username
        -String email
        -String passwordHash
        -UserStatus status
        -Instant lastLoginAt
        +isActive() boolean
        +lock(String reason) void
    }

    class Role {
        -Long id
        -String code
        -String name
    }

    class Permission {
        -Long id
        -String code
        -String module
    }

    class Faculty {
        -Long id
        -String code
        -String name
        -CatalogStatus status
    }

    class Department {
        -Long id
        -String code
        -String name
    }

    class Major {
        -Long id
        -String code
        -String name
        -Integer totalCreditsRequired
    }

    class AcademicClass {
        -Long id
        -String code
        -String name
        -Integer cohortYear
    }

    class Lecturer {
        -Long id
        -String lecturerCode
        -String fullName
        -String degree
        -String academicRank
    }

    class Student {
        -Long id
        -String studentCode
        -String fullName
        -LocalDate dateOfBirth
        -String nationalId
        -Integer cohortYear
        -AcademicStatus academicStatus
        +canEnroll() boolean
        +updateContactInfo(String phone, String email, String address) void
    }

    class Course {
        -Long id
        -String code
        -String name
        -Integer credits
        -Integer theoryHours
        -Integer practiceHours
        +validateTotalWeight() boolean
    }

    class CoursePrerequisite {
        -Long id
        -PrerequisiteType conditionType
    }

    class GradeComponent {
        -Long id
        -String code
        -String name
        -Integer weightPercent
        -Boolean isFinalExam
        -BigDecimal minThreshold
    }

    class Curriculum {
        -Long id
        -String code
        -String name
        -Integer cohortYear
        -Integer totalCredits
    }

    class CurriculumCourse {
        -Long id
        -Integer recommendedSemester
        -Boolean isRequired
    }

    class Semester {
        -Long id
        -String code
        -String name
        -TermType termType
        -Integer minCredits
        -Integer maxCredits
        -Instant registrationStartAt
        -Instant registrationEndAt
        -Boolean isRegistrationOpen
        +isRegistrationActive(Instant now) boolean
    }

    class Classroom {
        -Long id
        -String code
        -String building
        -Integer capacity
        -RoomType roomType
    }

    class CourseSection {
        -Long id
        -String sectionCode
        -Integer groupNumber
        -Integer maxCapacity
        -Integer currentEnrolled
        -GradeBookStatus gradeStatus
        -SectionStatus status
        +hasAvailableSeat() boolean
        +incrementEnrolled() void
        +decrementEnrolled() void
        +publishGrades() void
        +lockGrades() void
    }

    class SectionSchedule {
        -Long id
        -Integer dayOfWeek
        -Integer startPeriod
        -Integer endPeriod
        -Integer startWeek
        -Integer endWeek
        +overlapsWith(SectionSchedule other) boolean
    }

    class Enrollment {
        -Long id
        -EnrollmentStatus status
        -Integer attemptNumber
        -Instant enrolledAt
        -BigDecimal finalScore10
        -String letterGrade
        -BigDecimal gpaScore4
        -Boolean isPassed
        +calculateFinalResult(List~GradingScale~ scales) void
        +cancel(Long actorUserId, String reason) void
    }

    class StudentGrade {
        -Long id
        -BigDecimal score
    }

    User "1" -- "1..*" Role : user_roles
    Role "1..*" -- "1..*" Permission : role_permissions
    User "1" -- "0..1" Student : gắn với
    User "1" -- "0..1" Lecturer : gắn với

    Faculty "1" o-- "1..*" Department : quản lý
    Faculty "1" o-- "1..*" Major : quản lý
    Department "1" o-- "0..*" Lecturer : trực thuộc
    Department "1" o-- "0..*" Course : phụ trách
    Major "1" o-- "1..*" AcademicClass : gồm
    AcademicClass "1" o-- "0..*" Student : quản lý

    Course "1" *-- "1..*" GradeComponent : cấu thành điểm
    Course "1" --> "0..*" CoursePrerequisite : có điều kiện
    Major "1" --> "1..*" Curriculum : áp dụng
    Curriculum "1" *-- "1..*" CurriculumCourse : gồm
    Course "1" <-- "0..*" CurriculumCourse : tham chiếu

    Course "1" <-- "0..*" CourseSection : mở lớp
    Semester "1" <-- "0..*" CourseSection : trong kỳ
    Lecturer "0..1" <-- "0..*" CourseSection : giảng dạy
    CourseSection "1" *-- "1..*" SectionSchedule : có lịch học
    Classroom "1" <-- "0..*" SectionSchedule : xếp tại

    Student "1" --> "0..*" Enrollment : đăng ký
    CourseSection "1" --> "0..*" Enrollment : chứa
    Enrollment "1" *-- "0..*" StudentGrade : gồm điểm TP
    GradeComponent "1" <-- "0..*" StudentGrade : theo đầu điểm
```

---

### 1.3. Software Design Class Diagram (Mức thiết kế phần mềm – Phân hệ Đăng ký & Điểm trong Spring Boot)

```mermaid
classDiagram
    direction TB

    class EnrollmentController {
        -EnrollmentService enrollmentService
        +enroll(UserPrincipal user, EnrollRequestDto dto) ResponseEntity~EnrollmentResponseDto~
        +cancelMyEnrollment(UserPrincipal user, Long enrollmentId) ResponseEntity~Void~
        +overrideEnrollment(UserPrincipal aaUser, OverrideEnrollRequestDto dto) ResponseEntity~EnrollmentResponseDto~
    }

    class GradeController {
        -GradeService gradeService
        +saveSectionGrades(UserPrincipal lecUser, Long sectionId, SaveGradesRequestDto dto) ResponseEntity~SectionGradeBookDto~
        +publishGrades(UserPrincipal user, Long sectionId) ResponseEntity~Void~
        +lockGrades(UserPrincipal aaUser, Long sectionId) ResponseEntity~Void~
        +overrideScore(UserPrincipal aaUser, Long enrollmentId, OverrideScoreRequestDto dto) ResponseEntity~Void~
    }

    class EnrollmentService {
        -StudentRepository studentRepo
        -CourseSectionRepository sectionRepo
        -EnrollmentRepository enrollmentRepo
        -PrerequisiteValidator prerequisiteValidator
        -ScheduleConflictValidator scheduleValidator
        -AuditLogService auditLogService
        +enrollStudent(Long userId, Long sectionId) EnrollmentResponseDto
        +cancelStudentEnrollment(Long userId, Long enrollmentId) void
        +overrideEnrollment(Long aaUserId, OverrideEnrollRequestDto dto) EnrollmentResponseDto
    }

    class GradeService {
        -CourseSectionRepository sectionRepo
        -EnrollmentRepository enrollmentRepo
        -StudentGradeRepository studentGradeRepo
        -GradingScaleRepository gradingScaleRepo
        -GradeCalculatorDomainService gradeCalculator
        -AuditLogService auditLogService
        +saveGrades(Long lecturerUserId, Long sectionId, SaveGradesRequestDto dto) SectionGradeBookDto
        +publishSectionGrades(Long actorUserId, Long sectionId) void
        +lockSectionGrades(Long aaUserId, Long sectionId) void
    }

    class ScheduleConflictValidator {
        +hasClassroomConflict(Long semesterId, Long classroomId, SectionSchedule candidate) boolean
        +hasLecturerConflict(Long semesterId, Long lecturerId, SectionSchedule candidate) boolean
        +hasStudentConflict(Long studentId, Long semesterId, List~SectionSchedule~ candidateSchedules) boolean
    }

    class GradeCalculatorDomainService {
        +computeFinalGrade(Enrollment enrollment, List~GradeComponent~ components, List~GradingScale~ scales) GradeCalculationResult
        +computeStudentCpa(List~Enrollment~ publishedEnrollments, RetakePolicy policy) CpaSummary
    }

    class CourseSectionRepository {
        <<interface>>
        +findByIdForUpdate(Long id) Optional~CourseSection~
        +findBySemesterAndFilters(Long semesterId, Pageable pageable) Page~CourseSection~
    }

    class EnrollmentRepository {
        <<interface>>
        +existsActiveByStudentAndCourseAndSemester(Long studentId, Long courseId, Long semesterId) boolean
        +sumActiveCreditsByStudentAndSemester(Long studentId, Long semesterId) Integer
        +findAllByStudentIdWithDetails(Long studentId) List~Enrollment~
    }

    EnrollmentController --> EnrollmentService : gọi
    GradeController --> GradeService : gọi
    EnrollmentService --> CourseSectionRepository : khóa dòng FOR UPDATE
    EnrollmentService --> EnrollmentRepository : đọc/ghi
    EnrollmentService --> ScheduleConflictValidator : kiểm tra trùng lịch
    GradeService --> CourseSectionRepository : đọc/ghi
    GradeService --> EnrollmentRepository : cập nhật kết quả
    GradeService --> GradeCalculatorDomainService : tính điểm & GPA
```

#### Mã nguồn PlantUML (Class Diagram)
```plantuml
@startuml Software_Design_Class_Diagram
skinparam classAttributeIconSize 0

package "controller" {
  class EnrollmentController {
    - enrollmentService: EnrollmentService
    + enroll(user: UserPrincipal, dto: EnrollRequestDto): ResponseEntity<EnrollmentResponseDto>
    + cancelMyEnrollment(user: UserPrincipal, enrollmentId: Long): ResponseEntity<Void>
    + overrideEnrollment(aaUser: UserPrincipal, dto: OverrideEnrollRequestDto): ResponseEntity<EnrollmentResponseDto>
  }

  class GradeController {
    - gradeService: GradeService
    + saveSectionGrades(user: UserPrincipal, sectionId: Long, dto: SaveGradesRequestDto): ResponseEntity<SectionGradeBookDto>
    + publishGrades(user: UserPrincipal, sectionId: Long): ResponseEntity<Void>
    + lockGrades(aaUser: UserPrincipal, sectionId: Long): ResponseEntity<Void>
  }
}

package "service" {
  class EnrollmentService {
    - sectionRepo: CourseSectionRepository
    - enrollmentRepo: EnrollmentRepository
    - scheduleValidator: ScheduleConflictValidator
    - auditLogService: AuditLogService
    + enrollStudent(userId: Long, sectionId: Long): EnrollmentResponseDto
    + cancelStudentEnrollment(userId: Long, enrollmentId: Long): void
  }

  class GradeService {
    - sectionRepo: CourseSectionRepository
    - enrollmentRepo: EnrollmentRepository
    - gradeCalculator: GradeCalculatorDomainService
    + saveGrades(userId: Long, sectionId: Long, dto: SaveGradesRequestDto): SectionGradeBookDto
    + publishSectionGrades(userId: Long, sectionId: Long): void
    + lockSectionGrades(userId: Long, sectionId: Long): void
  }

  class ScheduleConflictValidator {
    + hasClassroomConflict(semesterId: Long, classroomId: Long, candidate: SectionSchedule): boolean
    + hasLecturerConflict(semesterId: Long, lecturerId: Long, candidate: SectionSchedule): boolean
    + hasStudentConflict(studentId: Long, semesterId: Long, schedules: List<SectionSchedule>): boolean
  }

  class GradeCalculatorDomainService {
    + computeFinalGrade(enrollment: Enrollment, components: List<GradeComponent>, scales: List<GradingScale>): GradeCalculationResult
    + computeStudentCpa(enrollments: List<Enrollment>, policy: RetakePolicy): CpaSummary
  }
}

package "repository" {
  interface CourseSectionRepository {
    + findByIdForUpdate(id: Long): Optional<CourseSection>
  }
  interface EnrollmentRepository {
    + existsActiveByStudentAndCourseAndSemester(studentId: Long, courseId: Long, semesterId: Long): boolean
    + sumActiveCreditsByStudentAndSemester(studentId: Long, semesterId: Long): Integer
  }
}

EnrollmentController --> EnrollmentService
GradeController --> GradeService
EnrollmentService --> CourseSectionRepository
EnrollmentService --> EnrollmentRepository
EnrollmentService --> ScheduleConflictValidator
GradeService --> CourseSectionRepository
GradeService --> EnrollmentRepository
GradeService --> GradeCalculatorDomainService
@enduml
```

---

## 2. STATE DIAGRAMS (VÒNG ĐỜI TRẠNG THÁI 5 ĐỐI TƯỢNG — MỤC 6.7)

### 2.1. State Diagram 1: Trạng thái Tài khoản (`User.status`)
- **Mục đích:** Mô tả vòng đời tài khoản đăng nhập và tác động thu hồi Refresh Token khi bị khóa (`BR-02`).

```mermaid
stateDiagram-v2
    [*] --> ACTIVE : Khởi tạo tài khoản (Admin / Tạo SV, GV)
    ACTIVE --> LOCKED : Admin khóa tài khoản\n[Thu hồi toàn bộ RefreshToken + Ghi AuditLog]
    LOCKED --> ACTIVE : Admin mở khóa tài khoản\n[Ghi AuditLog]
    ACTIVE --> INACTIVE : Ngưng sử dụng vĩnh viễn\n(SV tốt nghiệp lâu năm / GV nghỉ việc)
    INACTIVE --> [*]
```

---

### 2.2. State Diagram 2: Trạng thái Học vụ của Sinh viên (`Student.academic_status`)
- **Mục đích:** Mô tả 5 trạng thái học vụ của sinh viên (`BR-08`, `BR-19`) và điều kiện chuyển trạng thái do Phòng đào tạo thực hiện.

```mermaid
stateDiagram-v2
    [*] --> ACTIVE : Tiếp nhận hồ sơ nhập học\n[Được phép đăng ký học phần]
    ACTIVE --> ON_LEAVE : Quyết định bảo lưu\n[Chặn đăng ký học phần mới]
    ON_LEAVE --> ACTIVE : Tiếp nhận quay lại học tập
    ACTIVE --> SUSPENDED : Quyết định đình chỉ học tập có thời hạn\n[Chặn đăng ký học phần mới]
    SUSPENDED --> ACTIVE : Hết hạn đình chỉ, phục hồi học tập
    ACTIVE --> DROPPED_OUT : Quyết định thôi học / buộc thôi học
    ON_LEAVE --> DROPPED_OUT : Quá hạn bảo lưu không quay lại
    SUSPENDED --> DROPPED_OUT : Vi phạm kỷ luật mức buộc thôi học
    ACTIVE --> GRADUATED : Đủ điều kiện công nhận tốt nghiệp
    GRADUATED --> [*]
    DROPPED_OUT --> [*]
```

---

### 2.3. State Diagram 3: Trạng thái Vận hành Lớp học phần (`CourseSection.status`)
- **Mục đích:** Mô tả trạng thái mở/đóng/hủy của một lớp học phần trong học kỳ.

```mermaid
stateDiagram-v2
    [*] --> OPEN : Phòng Đào tạo mở lớp học phần\n(current_enrolled = 0)
    OPEN --> CLOSED : Đóng đợt đăng ký học phần\nhoặc PĐT khóa nhận đăng ký mới
    CLOSED --> OPEN : PĐT mở lại đợt đăng ký bổ sung
    OPEN --> CANCELLED : PĐT hủy lớp do không đủ sĩ số tối thiểu\n[Chuyển các Enrollment sang CANCELLED]
    CLOSED --> COMPLETED : Học kỳ kết thúc & Bảng điểm đã LOCKED
    CANCELLED --> [*]
    COMPLETED --> [*]
```

---

### 2.4. State Diagram 4: Trạng thái Lượt Đăng ký học phần (`Enrollment.status`)
- **Mục đích:** Thể hiện vòng đời của một bản ghi đăng ký học phần (`BR-21`, `BR-26`), không xóa vật lý khi hủy.

```mermaid
stateDiagram-v2
    [*] --> ENROLLED : SV tự đăng ký (thỏa 7 điều kiện)\nhoặc PĐT đăng ký hộ\n[current_enrolled += 1]
    ENROLLED --> CANCELLED : SV tự hủy (trong đợt ĐK)\nhoặc PĐT hủy hộ (có lý do)\n[current_enrolled -= 1]
    CANCELLED --> ENROLLED : SV đăng ký lại chính lớp đó\ntrong đợt đăng ký (thỏa 7 điều kiện)\n[current_enrolled += 1]
    ENROLLED --> COMPLETED_PASS : Bảng điểm PUBLISHED/LOCKED\n[is_passed == true (Điểm >= D & Cuối kỳ >= 4.0)]
    ENROLLED --> COMPLETED_FAIL : Bảng điểm PUBLISHED/LOCKED\n[is_passed == false (Điểm F hoặc Điểm liệt)]
    COMPLETED_PASS --> [*]
    COMPLETED_FAIL --> [*]
```

---

### 2.5. State Diagram 5: Trạng thái Bảng điểm Lớp học phần (`CourseSection.grade_status`)
- **Mục đích:** Mô tả quy trình kiểm soát điểm 3 bước `DRAFT → PUBLISHED → LOCKED` (`BR-30`).

```mermaid
stateDiagram-v2
    [*] --> DRAFT : Khi Lớp học phần được tạo\n[GV phụ trách được nhập/sửa điểm, SV chưa thấy]
    DRAFT --> DRAFT : Giảng viên lưu nháp / import điểm thành phần\n[Hệ thống tự tính điểm tổng kết tạm thời]
    DRAFT --> PUBLISHED : Giảng viên (hoặc PĐT) bấm "Công bố điểm"\n[Điều kiện: 100% SV ENROLLED đã đủ đầu điểm]\n[SV xem được điểm; GV không tự sửa được nữa]
    PUBLISHED --> LOCKED : Phòng Đào tạo bấm "Khóa sổ điểm" cuối kỳ\n[Chốt kết quả học kỳ]
    PUBLISHED --> PUBLISHED : PĐT sửa điểm đặc biệt (Override)\n[Bắt buộc nhập lý do + Ghi AuditLog]
    LOCKED --> LOCKED : PĐT sửa điểm đặc biệt sau khóa\n[Bắt buộc nhập lý do + Ghi AuditLog]
```

#### Mã nguồn PlantUML (Tổng hợp các State Diagrams)
```plantuml
@startuml State_GradeBook_And_Enrollment
state "Vòng đời Bảng điểm Lớp học phần (grade_status)" as GradeBookLifecycle {
  [*] --> DRAFT : Mở lớp học phần
  DRAFT --> DRAFT : GV nhập/sửa điểm (0..10)
  DRAFT --> PUBLISHED : GV/PĐT Công bố\n[100% SV đủ đầu điểm]
  PUBLISHED --> LOCKED : PĐT Khóa sổ cuối kỳ
  PUBLISHED --> PUBLISHED : PĐT Override có lý do + AuditLog
  LOCKED --> LOCKED : PĐT Override có lý do + AuditLog
}

state "Vòng đời Đăng ký học phần (Enrollment.status)" as EnrollmentLifecycle {
  [*] --> ENROLLED : Đăng ký thành công (+1 sĩ số)
  ENROLLED --> CANCELLED : Hủy đăng ký (-1 sĩ số)
  CANCELLED --> ENROLLED : Đăng ký lại (+1 sĩ số)
}
@enduml
```

---

## 3. COMPONENT DIAGRAM (SƠ ĐỒ THÀNH PHẦN HỆ THỐNG — MỤC 6.8)

### 3.1. Mục đích & Phạm vi
Thể hiện các thành phần phần mềm của kiến trúc **Modular Monolith** (Frontend Next.js, Backend Spring Boot chia theo module nghiệp vụ, Database PostgreSQL, Flyway, SMTP/Mailpit) và giao thức kết nối giữa chúng.

### 3.2. Sơ đồ trực quan (Mermaid)
```mermaid
flowchart TB
    subgraph FE_LAYER["Frontend Application — Next.js (TypeScript + shadcn/ui)"]
        UI_AUTH["Module UI: Auth & Profile"]
        UI_AA["Module UI: Quản trị Đào tạo (PĐT)"]
        UI_LEC["Module UI: Cổng Giảng viên"]
        UI_STU["Module UI: Cổng Sinh viên (ĐKHP, TKB, Điểm)"]
        UI_ADM["Module UI: Quản trị Hệ thống (Admin)"]
        NEXT_PROXY["Next.js API Proxy (/api/v1/* Rewrites)"]
    end

    subgraph BE_LAYER["Backend Application — Java Spring Boot (Modular Monolith)"]
        SEC_FLT["Spring Security + JwtAuthFilter + RBAC/DataScope"]
        REST_API["REST Controllers + Bean Validation + OpenAPI"]

        subgraph DOMAIN_MODULES["Business Service Modules"]
            M_AUTH["Auth & RBAC Module"]
            M_ORG["Organization & Lecturer Module"]
            M_STU["Student Profile & Import Module"]
            M_CRS["Course, Prerequisite & Curriculum Module"]
            M_SEC["Semester, Section & Schedule Module"]
            M_ENR["Enrollment & Concurrency Module"]
            M_GRD["Grade, GPA/CPA & Scale Module"]
            M_RPT["Reporting & AuditLog Module"]
        end

        DAL["Data Access Layer (Spring Data JPA + Native SQL Queries)"]
        FLYWAY["Flyway Schema Migration Engine"]
    end

    subgraph INFRA_LAYER["Data & External Services"]
        PG[("PostgreSQL Database<br/>(28 Tables, ACID, Row Locks)")]
        MAIL["Mailpit / SMTP Server<br/>(Reset Password Email)"]
    end

    UI_AUTH & UI_AA & UI_LEC & UI_STU & UI_ADM --> NEXT_PROXY
    NEXT_PROXY -- "HTTP/JSON + HttpOnly Cookie" --> SEC_FLT
    SEC_FLT --> REST_API
    REST_API --> M_AUTH & M_ORG & M_STU & M_CRS & M_SEC & M_ENR & M_GRD & M_RPT
    M_AUTH & M_ORG & M_STU & M_CRS & M_SEC & M_ENR & M_GRD & M_RPT --> DAL
    DAL -- "JDBC / HikariCP Connection Pool" --> PG
    FLYWAY -- "Chạy DDL & Seed SQL khi khởi động" --> PG
    M_AUTH -- "JavaMailSender (SMTP)" --> MAIL
```

### 3.3. Mã nguồn PlantUML (Component Diagram)
```plantuml
@startuml Component_Diagram
skinparam componentStyle uml2

package "Frontend (Next.js App Router + TypeScript)" {
  [Auth & Profile UI] as FE_Auth
  [Academic Affairs Portal UI] as FE_AA
  [Lecturer Portal UI] as FE_Lec
  [Student Portal UI] as FE_Stu
  [Admin Portal UI] as FE_Adm
  [Next.js Rewrite Proxy (/api/v1/*)] as FE_Proxy
}

package "Backend (Spring Boot Modular Monolith)" {
  [Spring Security + JwtAuthFilter] as BE_Sec
  [REST API Controllers + Validation] as BE_Ctl
  [Auth & RBAC Service] as S_Auth
  [Organization & Student Service] as S_OrgStu
  [Course, Curriculum & Section Service] as S_CourseSec
  [Enrollment Transaction Service] as S_Enroll
  [Grade & GPA Calculation Service] as S_Grade
  [Reporting & Audit Service] as S_Audit
  [Spring Data JPA + Native SQL Repositories] as BE_Repo
  [Flyway Migration] as BE_Flyway
}

database "PostgreSQL 17" as PG
node "SMTP / Mailpit" as Mail

FE_Auth --> FE_Proxy
FE_AA --> FE_Proxy
FE_Lec --> FE_Proxy
FE_Stu --> FE_Proxy
FE_Adm --> FE_Proxy

FE_Proxy --> BE_Sec : HTTPS / JSON (HttpOnly Cookie)
BE_Sec --> BE_Ctl
BE_Ctl --> S_Auth
BE_Ctl --> S_OrgStu
BE_Ctl --> S_CourseSec
BE_Ctl --> S_Enroll
BE_Ctl --> S_Grade
BE_Ctl --> S_Audit

S_Auth --> BE_Repo
S_OrgStu --> BE_Repo
S_CourseSec --> BE_Repo
S_Enroll --> BE_Repo
S_Grade --> BE_Repo
S_Audit --> BE_Repo

BE_Repo --> PG : JDBC (HikariCP)
BE_Flyway --> PG : V1__init_schema.sql
S_Auth --> Mail : SMTP (Port 1025/587)
@enduml
```

---

## 4. DEPLOYMENT DIAGRAM (SƠ ĐỒ TRIỂN KHAI — MỤC 6.9)

### 4.1. Mục đích & Phạm vi
Thiết kế mô hình triển khai gọn nhẹ, thực tế cho dự án cá nhân theo đúng quyết định `D-08` (chạy hoàn chỉnh bằng **Docker Compose** trên máy cá nhân để phát triển/demo, và sẵn sàng đưa lên 1 VPS hoặc Cloud bất kỳ khi cần mà không dùng hạ tầng phức tạp như Kubernetes).

### 4.2. Sơ đồ trực quan (Mermaid)
```mermaid
flowchart LR
    subgraph CLIENT["Máy người dùng / Nhà tuyển dụng"]
        BROWSER["🌐 Web Browser<br/>(Desktop / Mobile)"]
    end

    subgraph DOCKER_HOST["Docker Host (Máy cá nhân của bạn hoặc 1 Cloud VPS)"]
        subgraph DOCKER_NET["Docker Bridge Network (ptit-net)"]
            NEXT_CNT["📦 Container: ptit-frontend<br/>Next.js Node Server<br/>Port: 3000"]
            BOOT_CNT["📦 Container: ptit-backend<br/>OpenJDK 25 + Spring Boot JAR<br/>Port: 8080"]
            PG_CNT[("📦 Container: ptit-postgres<br/>PostgreSQL 17 Alpine<br/>Port: 5432")]
            MAIL_CNT["📦 Container: ptit-mailpit<br/>Mailpit SMTP & Web UI<br/>Port: 1025 (SMTP) / 8025 (UI)"]
        end
        PG_VOL[("💾 Docker Volume: pg_data<br/>Lưu trữ bền vững dữ liệu DB")]
    end

    BROWSER -- "HTTP :3000 (Giao diện & /api/v1/*)" --> NEXT_CNT
    NEXT_CNT -- "Internal HTTP :8080/api/v1/*" --> BOOT_CNT
    BOOT_CNT -- "TCP :5432 (JDBC + HikariCP)" --> PG_CNT
    BOOT_CNT -- "TCP :1025 (SMTP)" --> MAIL_CNT
    PG_CNT --- PG_VOL
    BROWSER -. "HTTP :8025 (Xem hộp thư test quên MK)" .-> MAIL_CNT
```

### 4.3. Mã nguồn PlantUML (Deployment Diagram)
```plantuml
@startuml Deployment_Diagram
node "Client Device (Desktop / Mobile)" {
  artifact "Web Browser" as Browser
}

node "Docker Host (Local Workstation / Single VPS)" {
  node "Docker Network: ptit-net" {
    node "Container: ptit-frontend" {
      artifact "Next.js App (Port 3000)" as NextApp
    }
    node "Container: ptit-backend" {
      artifact "Spring Boot Fat JAR\nOpenJDK 25 (Port 8080)" as SpringApp
    }
    node "Container: ptit-postgres" {
      database "PostgreSQL 17 (Port 5432)" as PostgresDB
    }
    node "Container: ptit-mailpit" {
      artifact "Mailpit SMTP (1025) & Web UI (8025)" as Mailpit
    }
  }
  storage "Docker Named Volume: pg_data" as PgVolume
}

Browser --> NextApp : HTTP/HTTPS :3000
NextApp --> SpringApp : Internal Proxy :8080/api/v1/*
SpringApp --> PostgresDB : JDBC :5432
SpringApp --> Mailpit : SMTP :1025
PostgresDB --> PgVolume : Persist Data
Browser ..> Mailpit : View Reset Emails :8025
@enduml
```

---

## 5. CHECKLIST NGHIỆM THU GIAI ĐOẠN 3 (GATE G2-A)

- [x] **System Context Diagram (6.1):** Đã xác định rõ ranh giới hệ thống, 4 Actor và 2 tác nhân phụ trợ (SMTP, CSV/Excel).
- [x] **Use Case Diagrams (6.2 & 9.3.6):** Đã xây dựng Use Case tổng quan và 4 Use Case chi tiết (bao gồm Authentication & Authorization Use Case Diagram), dùng đúng quan hệ `<<include>>`, `<<extend>>` và Generalization.
- [x] **Use Case Specifications (6.3):** Đã đặc tả chi tiết các Use Case trọng tâm kèm Preconditions, Main/Alt/Exception Flows, Postconditions và truy vết `BR`/`FR`.
- [x] **Activity Diagrams (6.4):** Đã vẽ đủ 7 Activity Diagrams có Swimlane, nhánh kiểm tra điều kiện và xử lý lỗi.
- [x] **Sequence Diagrams (6.5 & 9.3.6):** Đã vẽ đủ 5 Sequence Diagrams (bao gồm Đăng nhập, Kiểm tra quyền RBAC + Data-Scope, Đăng ký học phần có `FOR UPDATE`, Nhập & Công bố điểm, Tra cứu kết quả học tập).
- [x] **Class Diagrams (6.6):** Đã xây dựng cả Class Diagram mức Phân tích nghiệp vụ và mức Thiết kế phần mềm Spring Boot.
- [x] **State Diagrams (6.7):** Đã vẽ đủ 5 State Diagrams (`User`, `Student`, `CourseSection`, `Enrollment`, `SectionGradeBook`).
- [x] **Component & Deployment Diagrams (6.8, 6.9):** Đã thiết kế kiến trúc Modular Monolith và mô hình triển khai Docker Compose thực tế.
