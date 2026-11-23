# GIAI ĐOẠN 9: HƯỚNG DẪN ĐÓNG GÓI DOCKER, DEPLOYMENT & HOÀN THIỆN PORTFOLIO

> [!NOTE]
> Tài liệu này thực hiện đầy đủ mục **12 (Deployment & Portfolio)** và mục **13 (Tiêu chuẩn chất lượng đầu ra)** của [Planning.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/Planning.md#L1060-L1133), bao gồm cấu hình Docker Compose, danh sách tài khoản demo giả lập, checklist 17 đầu ra Portfolio và cẩm nang trả lời phỏng vấn kỹ thuật từ chính thiết kế của dự án.

---

## 1. ĐÓNG GÓI MÔI TRƯỜNG CHẠY THỬ BẰNG DOCKER COMPOSE (`D-08`)

Theo quyết định `D-08`, dự án sử dụng `docker-compose.yml` ở thư mục gốc để bất kỳ nhà tuyển dụng hoặc người chấm bài nào cũng có thể dựng toàn bộ môi trường (PostgreSQL 17 + Mailpit + Backend Spring Boot + Frontend Next.js) chỉ bằng **1 lệnh duy nhất**.

### Mẫu cấu hình `docker-compose.yml` chuẩn cho dự án
```yaml
services:
  postgres:
    image: postgres:17-alpine
    container_name: ptit-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: ptit_training
      POSTGRES_USER: ptit_user
      POSTGRES_PASSWORD: ptit_secret_password
      TZ: Asia/Ho_Chi_Minh
    ports:
      - "5432:5432"
    volumes:
      - pg_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ptit_user -d ptit_training"]
      interval: 5s
      timeout: 5s
      retries: 5

  mailpit:
    image: axllent/mailpit:latest
    container_name: ptit-mailpit
    restart: unless-stopped
    ports:
      - "1025:1025" # Cổng SMTP cho Spring Boot gửi email quên mật khẩu
      - "8025:8025" # Giao diện Web xem email test tại http://localhost:8025

volumes:
  pg_data:
```

### Lộ trình khi bạn muốn đưa lên Cloud Online (sau khi hoàn tất code)
Nhờ đã đóng gói chuẩn Docker và tách biến môi trường (`SPRING_DATASOURCE_URL`, `JWT_SECRET`, `NEXT_PUBLIC_API_URL`), khi cần deploy công khai bạn có 2 lựa chọn rất gọn:
1. **Phương án Cloud miễn phí:** Deploy `frontend/` lên **Vercel**, `backend/` (Docker) lên **Render / Railway**, và PostgreSQL lên **Neon.tech / Supabase** (chạy tự động Flyway migration khi khởi động).
2. **Phương án 1 VPS (5 USD/tháng):** Thuê 1 máy ảo Ubuntu (DigitalOcean / Hetzner / Vultr), cài Docker Compose + Caddy/Nginx tự động cấp chứng chỉ HTTPS Let's Encrypt.

---

## 2. TÀI KHOẢN DEMO & DỮ LIỆU MẪU GIẢ LẬP (MỤC 12.13 & 12.14)

> [!IMPORTANT]
> Toàn bộ dữ liệu dưới đây là **dữ liệu giả lập 100%**, không chứa bất kỳ thông tin cá nhân nhạy cảm hay dữ liệu sinh viên thật nào của PTIT.

| Vai trò (Role) | Tên đăng nhập (`username`) | Email đăng nhập | Mật khẩu Demo | Kịch bản trải nghiệm dành cho Nhà tuyển dụng |
|---|---|---|---|---|
| **Admin** (`ADMIN`) | `admin` | `admin@ptit-demo.edu.vn` | `Ptit@12345` | Quản lý tài khoản, khóa/mở khóa tài khoản, phân quyền Role-Permission, xem Nhật ký kiểm toán (`AuditLog`) |
| **Phòng Đào tạo** (`ACADEMIC_AFFAIRS`) | `pdt01` | `daotao01@ptit-demo.edu.vn` | `Ptit@12345` | Quản lý Khoa/Bộ môn/Ngành/Lớp HC, thêm/import Sinh viên, tạo Học phần & Tiên quyết, mở Lớp học phần (thử xếp trùng lịch để thấy hệ thống chặn), đăng ký hộ, khóa sổ điểm & xem Báo cáo thống kê |
| **Giảng viên** (`LECTURER`) | `GV001` | `gv.huy@ptit-demo.edu.vn` | `Ptit@12345` | Xem Thời khóa biểu giảng dạy, xem danh sách SV lớp `INT1306-20261-01`, nhập điểm thành phần, xem hệ thống tự tính điểm tổng kết & xét điểm liệt, bấm Công bố điểm |
| **Sinh viên 1** (`STUDENT` – Đủ ĐK tiên quyết) | `B22DCCN001` | `b22dccn001@stu.ptit-demo.vn` | `Ptit@12345` | Đã qua môn `INT1154` (điểm A) ở kỳ trước → Đăng ký/hủy học phần kỳ `20261`, xem Thời khóa biểu lưới 12 tiết, xem Bảng điểm & GPA/CPA, tự cập nhật SĐT/địa chỉ |
| **Sinh viên 2** (`STUDENT` – Chưa học tiên quyết) | `B22DCCN002` | `b22dccn002@stu.ptit-demo.vn` | `Ptit@12345` | Chưa học môn tiên quyết `INT1154` → Thử bấm đăng ký môn `INT1306` để kiểm chứng hệ thống chặn và báo lỗi thiếu môn tiên quyết |

---

## 3. CẤU TRÚC TÀI LIỆU REPOSITORY ĐỂ NHÀ TUYỂN DỤNG DỄ ĐÁNH GIÁ (MỤC 12)

Toàn bộ 17 đầu ra yêu cầu tại mục 12 của `Planning.md` được tổ chức khoa học trong thư mục `docs/` và `README.md` của dự án:

```text
f:\QUẢN-LÝ-SINH-VIÊN-2\
├── README.md                                                       # [Đầu ra 3, 4, 5, 6, 12, 13] Trang giới thiệu tổng quan dự án cho Nhà tuyển dụng
├── Planning.md                                                     # Đề bài & Yêu cầu gốc
└── docs/
    ├── 00-project-plan/
    │   ├── PROJECT_PLAN.md                                         # Kế hoạch tổng thể v1.0 & Nhật ký 24 quyết định (D-01..D-24)
    │   └── 02-STAGE7-PRODUCT-BACKLOG-AND-TRACEABILITY.md           # Product Backlog (Slices S0..S8) & Ma trận truy vết (BR ↔ FR ↔ UC ↔ DB ↔ API ↔ TC)
    ├── 01-requirements/
    │   ├── 01-STAGE1-DISCOVERY-AND-BRD.md                          # [Đầu ra 4, 5] Project Overview, Stakeholder, Actor, 13 Phân hệ, BRD, 32 Business Rules
    │   └── 02-STAGE2-REQUIREMENT-SPECIFICATION.md                  # 30 Functional Requirements, 10 NFRs, User Stories & Permission Matrix
    ├── 02-uml/
    │   ├── 01-CONTEXT-AND-USECASE-DIAGRAMS.md                      # [Đầu ra 8] System Context, 5 Use Case Diagrams & 6 Use Case Specifications
    │   ├── 02-ACTIVITY-AND-SEQUENCE-DIAGRAMS.md                    # [Đầu ra 8] 7 Activity Diagrams & 5 Sequence Diagrams (Mermaid + PlantUML)
    │   └── 03-CLASS-STATE-COMPONENT-DEPLOYMENT.md                  # [Đầu ra 7, 8] Class Diagrams, 5 State Diagrams, Component & Deployment Diagrams
    ├── 03-database/
    │   ├── 01-ENTITY-CONCEPTUAL-LOGICAL-ERD.md                     # [Đầu ra 9, 10] Phân tích 28 Entity, Conceptual, Logical & 5 sơ đồ ERD
    │   ├── 02-PHYSICAL-MODEL-AND-DATA-DICTIONARY.md                # [Đầu ra 10] Physical Data Model & Data Dictionary chi tiết 28 bảng
    │   ├── 03-NORMALIZATION-INDEXES-TRANSACTIONS.md                # [Đầu ra 10] Chuẩn hóa 1NF-3NF, Phi chuẩn hóa có kiểm soát, Index & Concurrency FOR UPDATE
    │   └── 04-POSTGRESQL-SCHEMA-AND-SEED.sql                       # [Đầu ra 10, 14] Script SQL DDL PostgreSQL + Indexes + Dữ liệu mẫu giả lập
    ├── 04-architecture/
    │   ├── 01-STAGE5-TECH-STACK-AND-ADR.md                         # [Đầu ra 6, 7] So sánh Tech Stack & Thiết kế Modular Monolith (8 Modules)
    │   └── 02-STAGE6-ARCHITECTURE-SECURITY-AND-API.md              # [Đầu ra 7, 11] Kiến trúc 5 tầng, Thiết kế Bảo mật & Đặc tả RESTful API
    ├── 06-testing/
    │   └── 01-STAGE8-TEST-PLAN-AND-TEST-CASES.md                   # [Đầu ra 16] Chiến lược kiểm thử 8 mức & 35 Test Cases trọng tâm
    └── 07-deployment/
        └── 01-STAGE9-DEPLOYMENT-AND-PORTFOLIO-GUIDE.md             # [Đầu ra 1, 2, 12, 13, 15, 17] Hướng dẫn Docker, Tài khoản Demo & Điểm nhấn phỏng vấn
```

---

## 4. CẨM NANG TRẢ LỜI PHỎNG VẤN TỪ DỰ ÁN NÀY (INTERVIEW TALKING POINTS)

Khi đưa dự án này vào CV và bước vào buổi phỏng vấn vị trí **Intern / Fresher / Junior Backend hoặc Fullstack Developer**, bạn có sẵn **5 câu chuyện kỹ thuật đắt giá** để trình bày:

1. **Cách xử lý tranh chấp đồng thời (Race Condition) khi đăng ký tín chỉ:**
   - *Trình bày:* Khi lớp học phần chỉ còn 1 suất cuối (`49/50`) và nhiều sinh viên bấm cùng lúc, nếu chỉ kiểm tra `if (current < max)` trong Java thì sẽ bị Overbooking. Em giải quyết bằng Transaction mức `READ COMMITTED` trên PostgreSQL kết hợp khóa bi quan theo thứ tự cố định: khóa bản ghi `Student` (`FOR UPDATE`) trước, sau đó khóa bản ghi `CourseSection` (`FOR UPDATE`), cộng thêm chốt chặn vật lý `CHECK (current_enrolled <= max_capacity)` và `Partial Unique Index` chống đăng ký trùng môn trong cùng học kỳ. Em đã kiểm chứng bằng Integration Test 20 luồng đồng thời trên Testcontainers.
2. **Thiết kế Database chuẩn 3NF kết hợp Phi chuẩn hóa có kiểm soát:**
   - *Trình bày:* Em tách bảng `grade_components` và `grading_scales` để không phải hardcode cột điểm hay công thức quy đổi. Đồng thời, em chủ đích giữ `current_enrolled` trên `course_sections` và `final_score_10 / gpa_score_4` trên `enrollments` để vừa tối ưu tốc độ đọc, vừa đóng băng kết quả lịch sử (Historical Snapshot) nếu sau này trường đổi quy chế.
3. **Thuật toán kiểm tra trùng lịch 3 chiều và đồ thị tiên quyết không chu trình:**
   - *Trình bày:* Em áp dụng công thức giao nhau của hai đoạn `[start1, end1]` và `[start2, end2]` (`start1 <= end2 AND start2 <= end1`) cho cả khoảng tuần và khoảng tiết để chặn trùng lịch cho Sinh viên, Giảng viên và Phòng học. Với môn tiên quyết, em dùng thuật toán DFS kiểm tra đồ thị có hướng không chu trình (DAG) trước khi lưu.
4. **Thiết kế Bảo mật 2 lớp (RBAC + Data-Scope) và JWT Revocation:**
   - *Trình bày:* Em không lưu JWT trong `localStorage` (vì dễ bị XSS đánh cắp) mà đặt `Access Token` (15 phút) + `Refresh Token` (7 ngày, lưu hash SHA-256 trong DB) vào `HttpOnly` Cookie đi qua Next.js Proxy. Ở Backend, ngoài `@PreAuthorize` kiểm tra Permission, mọi Service đều kiểm tra `Data-Scope` (Giảng viên chỉ sửa được điểm lớp mình dạy, Sinh viên chỉ truy cập dữ liệu của `currentUserId` lấy từ JWT) để chống hoàn toàn lỗi IDOR.
5. **Tách quy tắc nghiệp vụ khỏi tham số cấu hình:**
   - *Trình bày:* Các con số dễ thay đổi theo quy chế (giới hạn tín chỉ từng kỳ, trọng số điểm từng môn, thang quy đổi điểm chữ, chính sách tính CPA khi học lại) đều được lưu trong Database thay vì hardcode trong code Java.
