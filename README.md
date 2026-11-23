# HỆ THỐNG QUẢN LÝ ĐÀO TẠO SINH VIÊN PTIT (PORTFOLIO PROJECT)

> **Dự án cá nhân (Software / Backend / Fullstack Developer Portfolio)**  
> **Tech Stack:** Java 21+ (Spring Boot 3.5.16 Modular Monolith) · Next.js 15 (React 19 + TypeScript + Tailwind CSS) · PostgreSQL 17 · Docker Compose

---

## 1. Giới thiệu bài toán
Hệ thống Quản lý Đào tạo Sinh viên PTIT số hóa chu trình đào tạo theo hệ thống tín chỉ tại trường đại học, tập trung giải quyết các bài toán kỹ thuật và nghiệp vụ cốt lõi:
- **Quản lý đăng ký học phần có kiểm soát đồng thời (Concurrency Control):** Kiểm tra đồng thời 7 quy tắc học vụ (trạng thái sinh viên, đợt đăng ký, sĩ số, chống trùng môn trong kỳ, môn tiên quyết/học trước, giới hạn tín chỉ tối đa, trùng lịch học) và sử dụng khóa bi quan `SELECT ... FOR UPDATE` kết hợp Partial Unique Index trên PostgreSQL để chống vượt sĩ số (No Overbooking) khi nhiều sinh viên tranh suất học cuối cùng.
- **Kiểm tra xung đột thời khóa biểu 3 chiều:** Chặn trùng lịch theo `(Thứ, Khoảng tuần, Khoảng tiết)` cho Sinh viên, Giảng viên và Phòng học; kiểm tra chống chu trình (DAG) cho đồ thị học phần tiên quyết.
- **Quản lý điểm 3 trạng thái & Tính GPA/CPA tự động:** Thành phần điểm linh hoạt theo từng môn, xét điểm liệt cuối kỳ (`< 4.0`), quy trình `DRAFT → PUBLISHED → LOCKED`, điều chỉnh điểm sau khóa có ghi `AuditLog`, và tính CPA tích lũy lấy điểm cao nhất khi học lại/cải thiện.
- **Bảo mật 2 lớp (RBAC + Data-Scope Authorization):** Xác thực bằng JWT Access Token (15 phút) + Refresh Token Rotation (7 ngày, lưu hash SHA-256 trong DB) qua `HttpOnly` Cookie; kiểm tra quyền chức năng kết hợp phạm vi sở hữu/phụ trách dữ liệu ở Backend.

> *Lưu ý:* Dự án sử dụng quy chế và dữ liệu mẫu hoàn toàn giả lập phục vụ mục đích học tập và xây dựng Portfolio, không sử dụng dữ liệu sinh viên thật của PTIT.

---

## 2. Danh mục Hồ sơ Phân tích, Thiết kế & Kế hoạch (Giai đoạn 1 → Giai đoạn 9)

Toàn bộ bộ tài liệu phân tích nghiệp vụ, đặc tả yêu cầu, sơ đồ UML, thiết kế Database, kiến trúc, API, Backlog và Test Plan đã được hoàn thiện tại thư mục [`docs/`](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs):

| Giai đoạn | Tài liệu | Nội dung chính |
|---|---|---|
| **Kế hoạch tổng thể** | [docs/00-project-plan/PROJECT_PLAN.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/00-project-plan/PROJECT_PLAN.md) | Lộ trình tổng thể, 24 quyết định cốt lõi (`D-01`..`D-24`), phân tích rủi ro, phạm vi MVP (M1/M2/M3), Git workflow |
| **Giai đoạn 1: Discovery & BRD** | [docs/01-requirements/01-STAGE1-DISCOVERY-AND-BRD.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/01-requirements/01-STAGE1-DISCOVERY-AND-BRD.md) | Project Overview, Stakeholder Analysis, Phân tích 7 Actor (chốt 4 Actor MVP), Phân tích 13 Phân hệ (MoSCoW), BRD & **32 Business Rules (`BR-01`..`BR-32`)** |
| **Giai đoạn 2: Requirement Specification** | [docs/01-requirements/02-STAGE2-REQUIREMENT-SPECIFICATION.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/01-requirements/02-STAGE2-REQUIREMENT-SPECIFICATION.md) | Đặc tả chi tiết **30 Functional Requirements (`FR-AUTH`..`FR-SYS`)**, **10 nhóm NFR**, **User Stories** và **Permission Matrix** |
| **Giai đoạn 3 (Phần 1): Context & Use Case** | [docs/02-uml/01-CONTEXT-AND-USECASE-DIAGRAMS.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/02-uml/01-CONTEXT-AND-USECASE-DIAGRAMS.md) | System Context Diagram, Use Case tổng quan + 4 Use Case chi tiết (gồm Auth/RBAC) & 6 Use Case Specifications |
| **Giai đoạn 3 (Phần 2): Activity & Sequence** | [docs/02-uml/02-ACTIVITY-AND-SEQUENCE-DIAGRAMS.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/02-uml/02-ACTIVITY-AND-SEQUENCE-DIAGRAMS.md) | **7 Activity Diagrams** (có Swimlane) & **5 Sequence Diagrams** (Mermaid + PlantUML source code) |
| **Giai đoạn 3 (Phần 3): Class, State, Component, Deployment** | [docs/02-uml/03-CLASS-STATE-COMPONENT-DEPLOYMENT.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/02-uml/03-CLASS-STATE-COMPONENT-DEPLOYMENT.md) | Domain & Software Class Diagrams, **5 State Diagrams**, Component Diagram & Deployment Diagram |
| **Giai đoạn 4 (Phần 1): Entity, Conceptual, Logical & ERD** | [docs/03-database/01-ENTITY-CONCEPTUAL-LOGICAL-ERD.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/03-database/01-ENTITY-CONCEPTUAL-LOGICAL-ERD.md) | Phân tích 28 Entities, Conceptual Model, Logical Model (`1:1`, `1:N`, `N:M`), **4 ERD phân hệ** & **Master ERD 28 bảng** |
| **Giai đoạn 4 (Phần 2): Physical Model & Data Dictionary** | [docs/03-database/02-PHYSICAL-MODEL-AND-DATA-DICTIONARY.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/03-database/02-PHYSICAL-MODEL-AND-DATA-DICTIONARY.md) | Đặc tả Physical Data Model & Data Dictionary từng cột của toàn bộ **28 bảng PostgreSQL** |
| **Giai đoạn 4 (Phần 3): Normalization, Index & Transaction** | [docs/03-database/03-NORMALIZATION-INDEXES-TRANSACTIONS.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/03-database/03-NORMALIZATION-INDEXES-TRANSACTIONS.md) | Phân tích chuẩn hóa `1NF–3NF`, 3 điểm phi chuẩn hóa có kiểm soát, 10 Index thực tế & Thiết kế Transaction `SELECT ... FOR UPDATE` chống Deadlock/Race Condition |
| **Giai đoạn 4 (Phần 4): SQL Schema & Seed Data** | [docs/03-database/04-POSTGRESQL-SCHEMA-AND-SEED.sql](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/03-database/04-POSTGRESQL-SCHEMA-AND-SEED.sql) | Script SQL PostgreSQL (`CREATE TABLE`, `PK/FK`, `CHECK`, Partial Unique Index, Composite Indexes & Dữ liệu mẫu giả lập) |
| **Giai đoạn 5: Tech Stack & Modular Monolith ADR** | [docs/04-architecture/01-STAGE5-TECH-STACK-AND-ADR.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/04-architecture/01-STAGE5-TECH-STACK-AND-ADR.md) | So sánh Frontend/Backend/DBMS theo 7 tiêu chí, Trade-off & Thiết kế 8 Module của Modular Monolith |
| **Giai đoạn 6: Architecture, Security & API Spec** | [docs/04-architecture/02-STAGE6-ARCHITECTURE-SECURITY-AND-API.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/04-architecture/02-STAGE6-ARCHITECTURE-SECURITY-AND-API.md) | Kiến trúc 5 tầng, Thiết kế Bảo mật (JWT Cookie, RBAC + Data-Scope, OWASP), Đặc tả toàn bộ RESTful API & Luồng xử lý thực tế |
| **Giai đoạn 7: Product Backlog & Traceability** | [docs/00-project-plan/02-STAGE7-PRODUCT-BACKLOG-AND-TRACEABILITY.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/00-project-plan/02-STAGE7-PRODUCT-BACKLOG-AND-TRACEABILITY.md) | Product Backlog chi tiết theo 8 lát cắt dọc (`S1`..`S8`) & Ma trận truy vết `BR ↔ FR ↔ UC ↔ DB ↔ API ↔ TC` |
| **Giai đoạn 8: Test Plan & Test Cases** | [docs/06-testing/01-STAGE8-TEST-PLAN-AND-TEST-CASES.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/06-testing/01-STAGE8-TEST-PLAN-AND-TEST-CASES.md) | Chiến lược kiểm thử 8 mức độ & **35 Test Cases** (Bảo mật, Nghiệp vụ, Điểm liệt, Đồng thời 20 luồng tranh suất cuối) |
| **Giai đoạn 9: Deployment & Portfolio Guide** | [docs/07-deployment/01-STAGE9-DEPLOYMENT-AND-PORTFOLIO-GUIDE.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/07-deployment/01-STAGE9-DEPLOYMENT-AND-PORTFOLIO-GUIDE.md) | Cấu hình `docker-compose.yml`, Bảng tài khoản Demo 4 vai trò & Cẩm nang trả lời phỏng vấn từ dự án |

---

## 3. Cấu trúc mã nguồn đã triển khai

```text
├── backend/                          # Spring Boot 3.5.16 (Java 21 bytecode, Modular Monolith)
│   ├── src/main/java/com/ptit/training/
│   │   ├── common/                   # ApiResponse, GlobalExceptionHandler, JWT Cookie Security, OpenAPI
│   │   ├── auth/                     # Auth, Refresh Token Rotation, Reset Password, Admin RBAC
│   │   ├── organization/             # Khoa, Bộ môn, Ngành, Lớp hành chính, Giảng viên
│   │   ├── student/                  # Hồ sơ SV, Học bạ trạng thái, Import CSV All-or-Nothing
│   │   ├── course/                   # Học phần, Tiên quyết (DAG), Thành phần điểm, CTĐT
│   │   ├── section/                  # Năm học, Học kỳ, Phòng học, Lớp học phần, Kiểm tra trùng lịch 3 chiều
│   │   ├── enrollment/               # Đăng ký tín chỉ (FOR UPDATE chống Overbooking), Tiến độ CTĐT
│   │   ├── grade/                    # Nhập/Công bố/Khóa điểm, Điểm liệt, Tính GPA/CPA, Thang điểm
│   │   └── reporting/                # Thống kê, Sĩ số, Phân bố điểm, AuditLog
│   ├── src/main/resources/
│   │   ├── application.yml           # Cấu hình PostgreSQL + Flyway + JWT + Swagger + Mailpit
│   │   └── db/migration/
│   │       ├── V1__init_schema.sql   # 28 bảng PostgreSQL + CHECK + Partial Unique Index
│   │       └── V2__seed_demo_data.sql# Dữ liệu mẫu PTIT giả lập
│   └── src/test/                     # Unit Tests (CPA, DAG) + Integration & Concurrency Tests
├── frontend/                         # Next.js 15 App Router + TypeScript + Tailwind CSS
│   └── src/app/
│       ├── login/                    # Đăng nhập (kèm nút điền nhanh tài khoản Demo) & Quên/Đặt lại MK
│       ├── student/                  # Cổng Sinh viên (Đăng ký Tín chỉ, TKB 12 tiết, Kết quả, CTĐT, Hồ sơ)
│       ├── lecturer/                 # Cổng Giảng viên (Lớp phụ trách, Nhập/Import/Công bố điểm, Lịch dạy)
│       ├── academic/                 # Cổng Phòng Đào tạo (Kế hoạch HK, Mở lớp, Duyệt/Khóa điểm, Thống kê)
│       └── admin/                    # Cổng Quản trị (Tài khoản, Quyền RBAC, Thang điểm, Audit Log)
├── docker-compose.yml                # PostgreSQL 17 + Mailpit (Fake SMTP UI ở port 8025)
└── .github/workflows/ci.yml          # GitHub Actions CI (Backend Maven Test + Frontend Typecheck/Build)
```

---

## 4. Hướng dẫn chạy dự án Local

### Yêu cầu môi trường
- **Java:** JDK 21 trở lên (`java -version`)
- **Maven:** 3.9+ (hoặc dùng `mvn.cmd` trong `.m2\wrapper` nếu chưa thêm `mvn` vào `PATH`)
- **Node.js:** 20+ / 22+ & `npm`
- **Database:** PostgreSQL 16/17/18 (hoặc Docker Desktop)

### Bước 1: Khởi tạo PostgreSQL (chọn 1 trong 2 cách)

**Cách A — Dùng Docker Compose (khuyên dùng, kèm Mailpit test email quên mật khẩu):**
```bash
docker compose up -d
```

**Cách B — Dùng PostgreSQL đã cài trực tiếp trên máy (port `5432`):**
Mở `psql` (với user `postgres`) và chạy:
```sql
CREATE USER ptit_user WITH PASSWORD 'ptit_secret_password';
CREATE DATABASE ptit_training OWNER ptit_user;
GRANT ALL PRIVILEGES ON DATABASE ptit_training TO ptit_user;
```
> Khi Backend khởi động lần đầu, Flyway (`V1__init_schema.sql` + `V2__seed_demo_data.sql`) và `DataInitializer` sẽ tự động tạo toàn bộ 28 bảng và dữ liệu mẫu.

### Bước 2: Chạy Backend (Spring Boot — port `8080`)
```powershell
cd backend
mvn spring-boot:run
```
*(Nếu `mvn` chưa có trong PATH trên Windows, có thể chạy: `& "C:\Users\<USER>\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run`)*

- **API Docs (Swagger UI):** `http://localhost:8080/swagger-ui.html`
- **Health Check:** `http://localhost:8080/actuator/health`

### Bước 3: Chạy Frontend (Next.js — port `3000`)
Mở terminal thứ 2:
```powershell
cd frontend
npm install
npm run dev
```
Truy cập ứng dụng tại **`http://localhost:3000`** (Next.js tự proxy `/api/v1/*` sang `http://localhost:8080` qua `next.config.ts`).

---

## 5. Tài khoản Demo (Mật khẩu chung: `Ptit@12345`)

| Vai trò | Username | Mô tả nhanh |
|---|---|---|
| `ADMIN` | `admin` | Quản trị người dùng, khóa/mở khóa, phân quyền RBAC, thang điểm, Audit Log |
| `ACADEMIC_AFFAIRS` | `pdt01` | Phòng Đào tạo: Học kỳ, Học phần, Mở lớp, Sinh viên, Khóa/Điều chỉnh điểm, Báo cáo |
| `LECTURER` | `GV001`, `GV002` | Giảng viên: Xem lớp phụ trách, Nhập/Import CSV/Công bố điểm, Lịch dạy |
| `STUDENT` | `B22DCCN001` | Sinh viên đã qua `INT1154` (dùng test đăng ký môn có tiên quyết, xem TKB/GPA/CPA) |
| `STUDENT` | `B22DCCN002` | Sinh viên chưa qua `INT1154` (dùng test chặn tiên quyết khi đăng ký `INT1306`) |

---

## 6. Kiểm thử tự động (Automated Tests & Build)

- **Backend (Unit + Integration + Concurrency 10 threads `H2 MODE=PostgreSQL`):**
  ```powershell
  cd backend
  mvn -B test
  ```
- **Frontend (TypeScript Check + Production Build):**
  ```powershell
  cd frontend
  npm run typecheck
  npm run build
  ```
