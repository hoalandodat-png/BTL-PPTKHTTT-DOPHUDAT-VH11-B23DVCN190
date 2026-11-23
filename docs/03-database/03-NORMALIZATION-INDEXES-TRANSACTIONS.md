# GIAI ĐOẠN 4 (PHẦN 3): CHUẨN HÓA DỮ LIỆU (1NF–3NF), CHIẾN LƯỢC INDEX & THIẾT KẾ TRANSACTION ĐỒNG THỜI

> [!NOTE]
> Tài liệu này thực hiện mục **7.6 (Database Normalization)**, **7.8 (Constraints và Index)** và **7.9 (Transaction và Data Integrity)** của [Planning.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/Planning.md#L548-L622).
> Mọi khái niệm lý thuyết đều được minh họa trực tiếp bằng các bảng thực tế của dự án PTIT theo đúng quy tắc hướng dẫn Intern/Fresher tại mục 14.

---

## 1. PHÂN TÍCH CHUẨN HÓA DỮ LIỆU — DATABASE NORMALIZATION (MỤC 7.6)

### 1.1. Tại sao cần chuẩn hóa? Các dị thường dữ liệu (Anomalies) nếu thiết kế sai
Giả sử một người mới học thiết kế một bảng gộp `student_course_registrations_bad` lưu tất cả thông tin đăng ký và điểm như sau:
`student_code | student_name | major_name | faculty_name | section_code | course_code | course_name | credits | schedules_text | component_scores_text`

Bảng thiết kế sai này sẽ gặp 4 vấn đề nghiêm trọng:
1. **Data Redundancy (Dư thừa dữ liệu):** Tên sinh viên, tên ngành, tên khoa, tên môn học, số tín chỉ lặp lại hàng chục lần mỗi khi sinh viên đăng ký môn mới.
2. **Insert Anomaly (Dị thường khi thêm):** Không thể thêm một Khoa mới hoặc một Học phần mới nếu chưa có sinh viên nào đăng ký học phần đó.
3. **Update Anomaly (Dị thường khi sửa):** Khi đổi tên học phần `INT1306` hoặc đổi số điện thoại của sinh viên, phải cập nhật hàng nghìn dòng; nếu sót 1 dòng thì dữ liệu mâu thuẫn.
4. **Delete Anomaly (Dị thường khi xóa):** Nếu sinh viên duy nhất đăng ký một lớp học phần hủy đăng ký và ta xóa dòng đó, ta mất luôn thông tin về lớp học phần và môn học đó.

---

### 1.2. Áp dụng Chuẩn 1NF, 2NF, 3NF trong Database của dự án

#### Chuẩn 1 (First Normal Form — 1NF): Tính nguyên tố của thuộc tính (Atomicity) & Không có nhóm lặp
- **Quy tắc:** Mỗi ô dữ liệu chỉ chứa **một giá trị nguyên tố (atomic value)**, không chứa danh sách phân cách bằng dấu phẩy và không tạo các cột lặp (`schedule_1`, `schedule_2` hoặc `score_1`, `score_2`, `score_3`).
- **Áp dụng trong dự án PTIT:**
  - **Lịch học:** Thay vì lưu cột `schedule_text = "Thứ 2 Tiết 1-3 Phòng 401-A2; Thứ 5 Tiết 7-9 Phòng 302-A3"` trong bảng `course_sections` (không thể dùng SQL để kiểm tra giao đoạn tiết học), ta tách thành bảng `section_schedules` với các cột nguyên tố `day_of_week`, `start_period`, `end_period`, `start_week`, `end_week`, `classroom_id`.
  - **Thành phần điểm:** Thay vì tạo cột cứng `attendance_score`, `midterm_score`, `final_score` trong bảng `enrollments` (bị thiếu cột nếu môn có thêm bài tập lớn, hoặc thừa cột nếu môn chỉ có 2 đầu điểm), ta tách thành bảng `grade_components` và `student_grades`.

#### Chuẩn 2 (Second Normal Form — 2NF): Loại bỏ phụ thuộc hàm bộ phận (Partial Dependency)
- **Quy tắc:** Bảng đã đạt 1NF và mọi thuộc tính không khóa phải phụ thuộc hàm **đầy đủ** vào toàn bộ khóa chính (đặc biệt quan trọng với các bảng có khóa chính ghép – Composite Key).
- **Áp dụng trong dự án PTIT:**
  - Xét quan hệ `N:M` giữa Chương trình đào tạo (`curriculum_id`) và Học phần (`course_id`) tại bảng `curriculum_courses`:
    - Các thuộc tính `recommended_semester` (học kỳ khuyến nghị của môn đó trong chương trình đó) và `is_required` (bắt buộc hay tự chọn trong chương trình đó) phụ thuộc vào **cả cặp** `(curriculum_id, course_id)` → đặt tại bảng `curriculum_courses` là đúng chuẩn 2NF.
    - Ngược lại, `course_name` và `credits` chỉ phụ thuộc vào `course_id` (phụ thuộc bộ phận nếu đặt vào `curriculum_courses`). Do đó, `name` và `credits` được giữ ở bảng `courses`, không lưu lặp trong `curriculum_courses`.

#### Chuẩn 3 (Third Normal Form — 3NF): Loại bỏ phụ thuộc hàm bắc cầu (Transitive Dependency)
- **Quy tắc:** Bảng đã đạt 2NF và không có thuộc tính không khóa nào phụ thuộc bắc cầu qua một thuộc tính không khóa khác (`PK → A → B`).
- **Áp dụng trong dự án PTIT:**
  - Trong bảng `departments`: `department_id → faculty_id → faculty_name`. Ta chỉ lưu `faculty_id` trong `departments`, còn `name` của Khoa nằm ở bảng `faculties`.
  - Trong bảng `course_sections`: `section_id → lecturer_id → lecturer_full_name, department_id`. Ta chỉ lưu `lecturer_id` trong `course_sections`, không lưu tên giảng viên trong bảng lớp học phần.

---

### 1.3. Phân tích Phi chuẩn hóa có kiểm soát (Controlled Denormalization) & Trade-off

Trong thực tế, một thiết kế Database xuất sắc không phải là chuẩn hóa mù quáng mà là **chuẩn hóa 3NF làm nền tảng, sau đó phi chuẩn hóa có chủ đích tại những điểm nóng về hiệu năng hoặc toàn vẹn giao dịch**, kèm cơ chế bảo vệ chặt chẽ. Dự án sử dụng **3 điểm phi chuẩn hóa có kiểm soát**:

| STT | Điểm phi chuẩn hóa có kiểm soát | Nếu thuần 3NF sẽ làm thế nào? | Tại sao chọn phi chuẩn hóa ở đây? (Lợi ích) | Rủi ro (Trade-off) & Cách khóa rủi ro |
|---|---|---|---|---|
| 1 | Cột `current_enrolled` trong bảng `course_sections` | Mỗi lần hiển thị danh sách lớp hoặc kiểm tra sĩ số phải chạy `SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'ENROLLED'` | 1. Hiển thị danh sách 200 lớp học phần mở trong kỳ đọc ngay `current_enrolled` mà không cần `COUNT(*)` trên hàng chục nghìn dòng.<br>2. Cho phép đặt ràng buộc cứng ở tầng DB: `CHECK (current_enrolled >= 0 AND current_enrolled <= max_capacity)` và khóa dòng `course_sections` bằng `FOR UPDATE` | **Rủi ro:** Lệch số đếm nếu code quên cộng/trừ.<br>**Cách bảo vệ:** Mọi thao tác đăng ký/hủy đều cập nhật `enrollments` và `current_enrolled` trong cùng 1 `@Transactional` sau khi đã `SELECT ... FOR UPDATE` dòng `course_sections` |
| 2 | Cột `course_id` và `semester_id` trong bảng `enrollments` (dù đã suy ra được từ `section_id → course_sections`) | Chỉ lưu `section_id`, khi cần kiểm tra trùng môn trong kỳ phải `JOIN course_sections` | PostgreSQL **không cho phép dùng `JOIN` bên trong `UNIQUE INDEX`**. Việc lưu `course_id` và `semester_id` ngay trên `enrollments` cho phép tạo **Partial Unique Index**:<br>`UNIQUE (student_id, course_id, semester_id) WHERE status = 'ENROLLED'`<br>Nhờ vậy, chính इंजन PostgreSQL chặn tuyệt đối 100% việc 1 sinh viên đăng ký 2 nhóm lớp của cùng 1 môn trong cùng học kỳ (`BR-21`)! | **Rủi ro:** `course_id`/`semester_id` trên `enrollments` không khớp với `course_sections`.<br>**Cách bảo vệ:** Khi `INSERT` vào `enrollments`, `EnrollmentService` luôn lấy `course_id` và `semester_id` trực tiếp từ đối tượng `CourseSection` vừa khóa |
| 3 | Các cột kết quả tổng kết (`final_score_10`, `letter_grade`, `gpa_score_4`, `is_passed`) lưu trên `enrollments` | Mỗi lần sinh viên xem bảng điểm lại phải `JOIN student_grades + grade_components + grading_scales` để tính lại từ đầu | 1. Tăng tốc độ truy vấn bảng điểm và tính GPA/CPA lên nhiều lần.<br>2. **Bảo toàn lịch sử (Historical Snapshot):** Nếu 2 năm sau nhà trường thay đổi trọng số môn học hoặc thay đổi bảng `grading_scales`, kết quả đã khóa (`LOCKED`) của các học kỳ cũ vẫn giữ nguyên giá trị lịch sử lúc công bố! | **Rủi ro:** Điểm thành phần sửa nhưng điểm tổng kết chưa tính lại.<br>**Cách bảo vệ:** `GradeService` luôn tính và cập nhật lại 4 cột này trong cùng Transaction mỗi khi lưu `student_grades` |

---

## 2. RÀNG BUỘC TOÀN VẸN & CHIẾN LƯỢC ĐÁNH INDEX (MỤC 7.8)

### 2.1. Tổng hợp các loại Ràng buộc (Constraints) trên PostgreSQL
1. **Primary Key (`PK`):** Mọi bảng đều có khóa chính (`BIGINT IDENTITY` hoặc Composite PK).
2. **Foreign Key (`FK`):**
   - Dùng `ON DELETE RESTRICT` cho toàn bộ các bảng danh mục và nghiệp vụ (`faculties`, `departments`, `majors`, `academic_classes`, `courses`, `semesters`, `classrooms`, `course_sections`, `students`) để ngăn chặn xóa nhầm dữ liệu gốc (`BR-07`).
   - Dùng `ON DELETE CASCADE` chỉ cho các bảng phụ thuộc hoàn toàn vào bản ghi cha (`user_roles`, `role_permissions`, `refresh_tokens`, `grade_components`, `section_schedules`, `student_grades`).
3. **Check Constraints (`CHECK`):**
   - `course_sections`: `CHECK (max_capacity > 0 AND current_enrolled >= 0 AND current_enrolled <= max_capacity)`
   - `section_schedules`: `CHECK (day_of_week BETWEEN 2 AND 7 AND start_period BETWEEN 1 AND 12 AND end_period BETWEEN start_period AND 12 AND start_week >= 1 AND end_week >= start_week)`
   - `student_grades`: `CHECK (score >= 0.0 AND score <= 10.0)`
   - `course_prerequisites`: `CHECK (course_id <> prerequisite_course_id)`

---

### 2.2. Thiết kế Index dựa trên Truy vấn thực tế (Query-Driven Indexing)

Không đánh Index tràn lan trên mọi cột (vì mỗi Index làm chậm thao tác `INSERT/UPDATE` và tốn bộ nhớ). Dưới đây là danh sách các **Composite Index & Partial Index** được thiết kế bám sát các truy vấn nóng nhất của hệ thống:

| STT | Tên Index | Bảng & Cột đánh Index | Loại Index | Phục vụ truy vấn thực tế nào? |
|---|---|---|---|---|
| 1 | `uq_enrollments_active_course_semester` | `enrollments (student_id, course_id, semester_id) WHERE status = 'ENROLLED'` | **Partial Unique Index** | Vừa chặn tuyệt đối đăng ký trùng môn trong cùng học kỳ (`BR-21`), vừa tăng tốc truy vấn danh sách môn đang học của sinh viên trong kỳ |
| 2 | `idx_enrollments_student_status` | `enrollments (student_id, status)` | Composite B-Tree | Truy vấn toàn bộ lịch sử học tập của 1 sinh viên để kiểm tra môn Tiên quyết (`BR-22`) và tính GPA/CPA tích lũy (`FR-GRD-05`) |
| 3 | `idx_enrollments_section_status` | `enrollments (section_id, status)` | Composite B-Tree | Giảng viên/Phòng đào tạo lấy danh sách sinh viên đang `ENROLLED` của một lớp học phần (`FR-SEC-03`, `FR-GRD-01`) |
| 4 | `idx_course_sections_semester_course` | `course_sections (semester_id, course_id, status)` | Composite B-Tree | Sinh viên tra cứu danh sách lớp học phần đang mở trong học kỳ hiện hành (`FR-SEC-03`) |
| 5 | `idx_course_sections_lecturer_semester` | `course_sections (lecturer_id, semester_id)` | Composite B-Tree | Giảng viên xem danh sách lớp phân công và kiểm tra trùng lịch giảng viên trong học kỳ (`BR-17`, `FR-SCH-02`) |
| 6 | `idx_section_schedules_classroom_day` | `section_schedules (classroom_id, day_of_week, start_week, end_week)` | Composite B-Tree | Kiểm tra nhanh trùng lịch Phòng học khi mở lớp học phần (`BR-16`, `FR-SEC-02`) |
| 7 | `idx_section_schedules_section` | `section_schedules (section_id, day_of_week)` | Composite B-Tree | Lấy lịch học của lớp học phần để kiểm tra trùng lịch Sinh viên khi đăng ký (`BR-24`) và hiển thị TKB |
| 8 | `idx_students_filter` | `students (major_id, academic_class_id, cohort_year, academic_status)` | Composite B-Tree | Phòng đào tạo lọc danh sách sinh viên theo Ngành, Lớp hành chính, Khóa, Trạng thái (`FR-STU-01`) |
| 9 | `idx_refresh_tokens_user_active` | `refresh_tokens (user_id) WHERE revoked_at IS NULL` | **Partial B-Tree Index** | Thu hồi tức thì toàn bộ các Refresh Token đang hoạt động khi Admin khóa tài khoản hoặc User đổi mật khẩu (`BR-02`) |
| 10 | `idx_audit_logs_entity_created` | `audit_logs (entity_type, entity_id, created_at DESC)` | Composite B-Tree | Admin tra cứu lịch sử kiểm toán theo đối tượng và thời gian mới nhất (`FR-SYS-02`) |

---

## 3. TRANSACTION, CONCURRENCY & DATA INTEGRITY (MỤC 7.9)

### 3.1. Bài toán thực tế: Hai sinh viên cùng bấm đăng ký suất học cuối cùng (Race Condition)

**Tình huống:** Lớp học phần `INT1306-20261-01` có `max_capacity = 50` và hiện tại `current_enrolled = 49` (chỉ còn đúng **1 chỗ trống cuối cùng**).
Cùng tại mili-giây `t0`, Sinh viên A và Sinh viên B đều bấm nút **"Đăng ký"**.

#### Điều gì xảy ra nếu KHÔNG dùng Khóa dòng (Lost Update / Overbooking)?
1. `Tx-A` (của SV A) đọc `course_sections`: thấy `current_enrolled = 49 < 50` → Hợp lệ!
2. `Tx-B` (của SV B) cũng đọc `course_sections` cùng lúc: vẫn thấy `current_enrolled = 49 < 50` (vì `Tx-A` chưa commit) → Hợp lệ!
3. `Tx-A` thêm bản ghi `enrollments` cho SV A và cập nhật `current_enrolled = 50` → Commit.
4. `Tx-B` thêm bản ghi `enrollments` cho SV B và cập nhật `current_enrolled = 51` → **Vượt sĩ số tối đa (Overbooking)!**

---

### 3.2. Phân tích các phương án xử lý Concurrency trong PostgreSQL

| Phương án | Cơ chế hoạt động | Ưu điểm | Nhược điểm | Quyết định cho dự án PTIT |
|---|---|---|---|---|
| **1. Pessimistic Locking (`SELECT ... FOR UPDATE`)** | Khi bắt đầu kiểm tra đăng ký, thực thi `SELECT * FROM course_sections WHERE id = :sectionId FOR UPDATE`. PostgreSQL đặt khóa độc quyền cấp dòng (`Row-level Exclusive Lock`) trên đúng dòng lớp học phần đó cho đến khi Transaction `COMMIT` hoặc `ROLLBACK` | • Tuần tự hóa chính xác các request nhắm vào **cùng 1 lớp học phần** (các lớp khác vẫn chạy song song bình thường).<br>• Không bắt các kiểm tra phức tạp phía sau phải chạy lại.<br>• Dễ hiểu, hành vi tất định (deterministic) | Các request cùng đăng ký 1 lớp tại cùng mili-giây sẽ xếp hàng đợi nhau vài mili-giây | ✅ **CHỌN LÀM CƠ CHẾ CHÍNH** (`D-01`, `BR-25`) |
| **2. Optimistic Locking (`@Version` column)** | Thêm cột `version` vào `course_sections`; lúc `UPDATE` kiểm tra `WHERE id = :id AND version = :oldVersion` | Không giữ khóa khi đọc | Khi đợt đăng ký mở, hàng chục SV cùng bấm vào 1 lớp hot sẽ làm 49/50 request bị `OptimisticLockingFailureException` dù lớp vẫn còn nhiều chỗ trống! | Không phù hợp cho bài toán cao điểm đăng ký tín chỉ |
| **3. `SERIALIZABLE` Isolation Level** | Đặt mức cô lập cao nhất cho toàn bộ transaction | Chặn mọi anomaly | Chi phí hiệu năng cao, phát sinh nhiều lỗi `40001 serialization_failure` phải retry phức tạp | Không cần thiết khi đã có `READ COMMITTED` + `FOR UPDATE` |

---

### 3.3. Giải pháp chuẩn của dự án trên PostgreSQL + Spring Boot

1. **Isolation Level:** Giữ mức mặc định chuẩn của PostgreSQL là **`READ COMMITTED`** kết hợp **`SELECT ... FOR UPDATE`** (trong Spring Data JPA dùng `@Lock(LockModeType.PESSIMISTIC_WRITE)` kèm `@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))`).
2. **Cách `READ COMMITTED` + `FOR UPDATE` xử lý kịch bản suất cuối cùng (`49/50`):**
   - `Tx-A` đến trước 0.1ms: chạy `SELECT ... FROM course_sections WHERE id = 10 FOR UPDATE` → Giữ khóa dòng `id = 10`.
   - `Tx-B` đến ngay sau: chạy `SELECT ... FROM course_sections WHERE id = 10 FOR UPDATE` → PostgreSQL tạm dừng `Tx-B` chờ khóa dòng `id = 10`.
   - `Tx-A` kiểm tra 7 điều kiện nghiệp vụ (mất ~5ms), `INSERT INTO enrollments`, `UPDATE course_sections SET current_enrolled = 50`, rồi **`COMMIT`**.
   - Ngay khi `Tx-A` commit, PostgreSQL đánh thức `Tx-B` và **tự động đọc lại phiên bản dòng mới nhất đã commit của `Tx-A`** (`current_enrolled = 50`).
   - `Tx-B` kiểm tra điều kiện `current_enrolled < max_capacity` (`50 < 50` là `false`) → Ném ngoại lệ `EnrollSectionFullException`, `ROLLBACK` ngay lập tức và trả về `HTTP 409 ENROLL_SECTION_FULL` cho Sinh viên B!
3. **Phòng chống trường hợp cùng 1 Sinh viên bấm đúp chuột 2 lần liên tiếp (Double-Click Race Condition):**
   - Nếu cùng 1 Sinh viên gửi 2 request đồng thời vào 2 nhóm lớp khác nhau (`INT1306-20261-01` và `INT1306-20261-02`) của cùng môn `INT1306`, khóa `FOR UPDATE` trên `course_sections` sẽ khóa 2 dòng khác nhau.
   - Do đó, taทำ thêm 2 lớp bảo vệ:
     1. Khóa dòng `Student` (`SELECT * FROM students WHERE id = :studentId FOR UPDATE`) ngay đầu transaction đăng ký để tuần tự hóa mọi thao tác đăng ký của **cùng một sinh viên** (theo thứ tự khóa cố định: **`students` trước → `course_sections` sau**, nhờ đó **không bao giờ xảy ra Deadlock**!).
     2. Chốt chặn cuối cùng ở tầng vật lý PostgreSQL bằng **Partial Unique Index** `uq_enrollments_active_course_semester ON enrollments (student_id, course_id, semester_id) WHERE status = 'ENROLLED'`.

> [!TIP]
> **Điểm nhấn khi trả lời phỏng vấn Backend:**
> Khi nhà tuyển dụng hỏi *"Nếu sinh viên mở 2 tab trình duyệt bấm đăng ký 2 lớp bị trùng lịch học cùng một lúc thì `FOR UPDATE` trên `CourseSection` có chặn được không?"*, bạn trả lời được ngay:
> *"Chỉ khóa `CourseSection` sẽ chỉ chặn được tranh chấp sĩ số trên cùng 1 lớp, nhưng không chặn được 1 sinh viên bấm đồng thời vào 2 lớp khác nhau bị trùng lịch. Vì vậy, trong `EnrollmentService`, em luôn khóa theo thứ tự cố định: khóa bản ghi `Student` (`FOR UPDATE`) trước rồi mới khóa `CourseSection` (`FOR UPDATE`). Cách này vừa tuần tự hóa mọi request của cùng 1 sinh viên để kiểm tra trùng lịch/vượt tín chỉ chính xác 100%, vừa tuân thủ thứ tự khóa đồng nhất (`Student → CourseSection`) nên triệt tiêu hoàn toàn nguy cơ Deadlock."*
