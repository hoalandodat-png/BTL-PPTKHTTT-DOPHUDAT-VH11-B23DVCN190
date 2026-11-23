# GIAI ĐOẠN 5: PHÂN TÍCH LỰA CHỌN TECH STACK & KIẾN TRÚC MODULAR MONOLITH (ADR-001)

> [!NOTE]
> Tài liệu này thực hiện đầy đủ mục **8.1 → 8.5** của [Planning.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/Planning.md#L641-L730), cung cấp bảng so sánh có cơ sở giữa các ứng viên Frontend, Backend, Database theo 7 tiêu chí, lý do chọn **Java Spring Boot + Next.js + PostgreSQL** (`D-04`), phân tích Trade-off và thiết kế cấu trúc module của **Modular Monolith**.

---

## 1. SO SÁNH FRONTEND (MỤC 8.1)

| Tiêu chí đánh giá | React / Next.js (TypeScript) | Vue 3 / Nuxt | Angular |
|---|---|---|---|
| **1. Độ khó tiếp cận (Learning Curve)** | Trung bình. Cú pháp JSX/TSX và Hooks trực quan; cần lưu ý tách bạch Server/Client Component trong App Router | Thấp – Trung bình. Cú pháp Single-File Component (`.vue`) dễ làm quen | Cao. Đòi hỏi nắm vững RxJS, Dependency Injection, Decorators ngay từ đầu |
| **2. Khả năng xây dựng giao diện quản trị (Admin/Portal UI)** | Rất mạnh nhờ kết hợp `shadcn/ui` + `TanStack Table` (phân trang, lọc, sắp xếp) + `React Hook Form` + `Zod` | Tốt (Element Plus, PrimeVue) | Rất tốt cho biểu mẫu lớn (Reactive Forms, Angular Material) |
| **3. Khả năng tái sử dụng Component** | Rất cao (Composition pattern + Custom Hooks) | Cao (Composables) | Cao (Standalone Components, Directives) |
| **4. Hệ sinh thái (Ecosystem) & Tài liệu** | Lớn nhất hiện nay; cộng đồng hỗ trợ cực kỳ phong phú | Lớn, tài liệu chính thức rõ ràng | Đầy đủ "all-in-one" từ Google |
| **5. Khả năng phát triển, bảo trì & Giá trị Portfolio** | Rất cao — nhu cầu tuyển dụng Fullstack (Spring Boot + React/Next.js) chiếm tỷ trọng lớn nhất trên thị trường | Trung bình – Cao | Cao ở một số khối ngân hàng/doanh nghiệp lớn |

**Kết luận Frontend:** Chọn **Next.js (React + TypeScript) + Tailwind CSS + `shadcn/ui` + TanStack Query/Table + React Hook Form/Zod**.

---

## 2. SO SÁNH BACKEND (MỤC 8.2)

| Tiêu chí đánh giá | Java Spring Boot | Node.js (NestJS / Express) | ASP.NET Core (C#) |
|---|---|---|---|
| **1. Xây dựng REST API & Validation** | Chuẩn mực với `Spring Web`, `Bean Validation (@Valid)` và `springdoc-openapi` | Tốt với NestJS (`class-validator`), cần cấu hình thêm nếu dùng Express thuần | Rất tốt, hiệu năng cao |
| **2. Xử lý Business Logic & OOP** | Mạnh mẽ, kiểu tĩnh chặt chẽ (Strong Typing), dễ mô hình hóa các quy tắc học vụ phức tạp | TypeScript hỗ trợ tốt nhưng kiểu bị xóa lúc runtime (Type Erasure) | Tương đương Java |
| **3. Authentication & Authorization (RBAC + Data Scope)** | **Spring Security** là tiêu chuẩn công nghiệp cho RBAC (`@PreAuthorize`) và custom security filter | Cần tự lắp ghép Passport/Guards nhiều hơn | Identity + Policy-based Auth mạnh |
| **4. ORM & Quản lý Transaction** | **Spring Data JPA (Hibernate)** + `@Transactional` cực kỳ chín muồi, hỗ trợ `@Lock(PESSIMISTIC_WRITE)` trực tiếp | Prisma / TypeORM hỗ trợ transaction nhưng xử lý khóa dòng phức tạp kém tự nhiên hơn JPA | Entity Framework Core rất tốt |
| **5. Kiểm thử (Testing)** | **JUnit 5 + Mockito + Spring Boot Test + Testcontainers** là bộ công cụ kiểm thử tích hợp mạnh nhất hiện nay | Jest + Supertest tốt | xUnit + Testcontainers tốt |
| **6. Giá trị thể hiện trong Portfolio** | Rất cao cho vị trí Backend / Fullstack Java Developer tại các công ty sản phẩm và doanh nghiệp lớn | Phù hợp startup JS/TS | Phù hợp hệ sinh thái Microsoft |

**Kết luận Backend:** Chọn **Java Spring Boot (Maven, Java 25 LTS)**.

---

## 3. SO SÁNH HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU — DATABASE (MỤC 8.3)

| Tiêu chí đánh giá | PostgreSQL | MySQL | SQL Server |
|---|---|---|---|
| **1. Ràng buộc dữ liệu (Constraints)** | **Mạnh nhất:** hỗ trợ đầy đủ `CHECK`, **Partial Unique Index** (`WHERE status = 'ENROLLED'`), `JSONB` | Hỗ trợ `CHECK` từ 8.0 nhưng **không có Partial Unique Index** trực tiếp (phải dùng workaround cột ảo) | Hỗ trợ Filtered Unique Index và Check tốt |
| **2. Transaction & Concurrency (`FOR UPDATE`)** | MVCC hiện đại, `READ COMMITTED` + `SELECT ... FOR UPDATE` hoạt động cực kỳ chính xác và ổn định | InnoDB hỗ trợ tốt nhưng mặc định `REPEATABLE READ` dễ phát sinh Gap Lock gây Deadlock hơn khi insert đồng thời | Tốt nhưng nặng tài nguyên hơn |
| **3. Index & Truy vấn thống kê** | Hỗ trợ B-Tree, Partial Index, GIN Index (`JSONB`), Window Functions, CTE rất mạnh cho báo cáo điểm/GPA | Tốt cho truy vấn OLTP đơn giản | Rất mạnh nhưng bản miễn phí (Express) giới hạn RAM/dung lượng |
| **4. Khả năng triển khai bằng Docker & Cloud** | Image `postgres:17-alpine` siêu nhẹ (~80MB RAM khi chạy local), miễn phí 100% | Nhẹ, phổ biến | Image Docker nặng (yêu cầu >= 2GB RAM), tốn tài nguyên máy cá nhân |

**Kết luận Database:** Chọn **PostgreSQL 17**.

---

## 4. TỔNG HỢP ĐÁNH GIÁ THEO 7 TIÊU CHÍ & TRADE-OFF (MỤC 8.4)

| # | Tiêu chí ưu tiên (`Planning.md` 8.4) | Đánh giá tổ hợp **Spring Boot + Next.js + PostgreSQL** |
|---|---|---|
| 1 | Phù hợp với dự án quản lý đào tạo | `@Transactional` + `FOR UPDATE` + Partial Unique Index của PostgreSQL giải quyết trọn vẹn bài toán đăng ký học phần và tính điểm |
| 2 | Phù hợp với 1 người phát triển | Monorepo rõ ràng, sinh OpenAPI tự động, UI dựng nhanh từ `shadcn/ui` |
| 3 | Dễ học và có tài liệu tốt | Cả 3 công nghệ đều có tài liệu chính thống và cộng đồng lớn nhất trong phân khúc |
| 4 | Dễ kiểm thử | Testcontainers khởi tạo PostgreSQL thật trong vài giây để chạy test đăng ký đồng thời |
| 5 | Dễ đóng gói & chạy thử | 1 file `docker-compose.yml` chạy toàn bộ hệ thống |
| 6 | Giá trị cao trong Portfolio | Tổ hợp công nghệ được săn đón nhiều nhất cho vị trí Intern/Fresher/Junior Backend & Fullstack |
| 7 | Có thể mở rộng về sau | Từ Modular Monolith có thể tách module hoặc mở rộng thêm phân hệ M3 dễ dàng |

### Phân tích Trade-off và Cách kiểm soát
- **Trade-off 1:** Chạy cả Spring Boot và Next.js cần quản lý 2 tiến trình khi phát triển.
  - *Cách kiểm soát:* Dùng Next.js `rewrites` chuyển tiếp `/api/v1/*` về `http://localhost:8080/api/v1/*` để trình duyệt chỉ làm việc với 1 origin duy nhất (`http://localhost:3000`), loại bỏ hoàn toàn rắc rối về CORS và Cookie khác domain.
- **Trade-off 2:** Nguy cơ đặt nhầm logic nghiệp vụ vào tầng Server của Next.js.
  - *Cách kiểm soát:* Áp dụng quy tắc kiến trúc nghiêm ngặt: **100% logic nghiệp vụ, validation dữ liệu cuối cùng và kiểm tra phân quyền đặt tại Spring Boot Backend**. Next.js chỉ đóng vai trò tầng trình diễn (Presentation Layer).

---

## 5. KIẾN TRÚC KHUYẾN NGHỊ: MODULAR MONOLITH (MỤC 8.5)

### 5.1. Tại sao chọn Modular Monolith thay vì Microservices?
- **Không chọn Microservices vì:** Một người phát triển sẽ bị sa lầy vào cấu hình mạng, phân tán giao dịch (Distributed Transaction / Saga), đồng bộ dữ liệu giữa `Student Service`, `Course Service`, `Enrollment Service`, làm mất đi lợi thế quan trọng nhất của bài toán đăng ký học phần là **ACID Transaction trên cùng một Database**.
- **Chọn Modular Monolith vì:** Toàn bộ ứng dụng đóng gói và triển khai như **một khối duy nhất (Single Deployable Unit)** dùng chung một PostgreSQL Database (đảm bảo ACID Transaction tuyệt đối), nhưng mã nguồn bên trong được chia thành **8 module nghiệp vụ có ranh giới trách nhiệm rõ ràng**.

### 5.2. Phân chia 8 Module nghiệp vụ trong Backend Spring Boot

```text
com.ptit.training
├── common/                 # Cấu hình dùng chung, Exception Handler, ApiResponse, PageResponse, Security Principal
├── auth/                   # Module 1: Đăng nhập, JWT, Refresh Token, Đổi/Quên mật khẩu, Quản lý User & RBAC
├── organization/           # Module 2: Khoa (Faculty), Bộ môn (Department), Ngành (Major), Lớp HC (AcademicClass), Giảng viên (Lecturer)
├── student/                # Module 3: Hồ sơ Sinh viên, Lịch sử trạng thái học vụ, Import Excel Sinh viên
├── course/                 # Module 4: Học phần (Course), Tiên quyết (Prerequisite), Thành phần điểm (GradeComponent), Chương trình đào tạo (Curriculum)
├── section/                # Module 5: Năm học, Học kỳ, Đợt đăng ký, Phòng học, Lớp học phần (CourseSection), Lịch học (SectionSchedule) & Kiểm tra trùng lịch
├── enrollment/             # Module 6: Đăng ký học phần, Hủy đăng ký, Đăng ký/Hủy hộ, Xử lý khóa dòng đồng thời (FOR UPDATE)
├── grade/                  # Module 7: Nhập/Import điểm thành phần, Tính điểm tổng kết, Công bố/Khóa điểm, Thang điểm, Tính GPA/CPA
└── reporting/              # Module 8: Báo cáo thống kê học vụ & Nhật ký kiểm toán (AuditLog)
```

| STT | Module | Trách nhiệm nghiệp vụ duy nhất (Single Responsibility) | Các bảng DB do Module sở hữu chính |
|---|---|---|---|
| 1 | `auth` | Xác thực, phát hành/thu hồi JWT & Refresh Token, quản lý tài khoản `User` và phân quyền `Role` – `Permission` | `users`, `roles`, `permissions`, `user_roles`, `role_permissions`, `refresh_tokens` |
| 2 | `organization` | Quản lý cơ cấu tổ chức học thuật và hồ sơ giảng viên | `faculties`, `departments`, `majors`, `academic_classes`, `lecturers` |
| 3 | `student` | Quản lý vòng đời hồ sơ sinh viên, trạng thái học vụ và import sinh viên | `students`, `student_status_history` |
| 4 | `course` | Quản lý danh mục học phần, kiểm tra đồ thị tiên quyết không chu trình, thành phần điểm và khung chương trình đào tạo | `courses`, `course_prerequisites`, `grade_components`, `curriculums`, `curriculum_courses` |
| 5 | `section` | Quản lý niên lịch học kỳ, đợt đăng ký, phòng học, mở lớp học phần, xếp lịch và kiểm tra trùng lịch 3 chiều | `academic_years`, `semesters`, `classrooms`, `course_sections`, `section_schedules` |
| 6 | `enrollment` | Thực thi giao dịch đăng ký và hủy đăng ký học phần có kiểm soát đồng thời | `enrollments` |
| 7 | `grade` | Nhập điểm, tính điểm tổng kết, xét điểm liệt, công bố/khóa sổ điểm và tính GPA/CPA | `student_grades`, `grading_scales`, `system_configs` |
| 8 | `reporting` | Truy vấn thống kê tổng hợp và quản lý nhật ký kiểm toán hệ thống | `audit_logs` (+ truy vấn đọc liên bảng) |
