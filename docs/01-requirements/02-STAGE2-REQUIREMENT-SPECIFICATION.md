# GIAI ĐOẠN 2: REQUIREMENT SPECIFICATION — ĐẶC TẢ YÊU CẦU HỆ THỐNG

> [!NOTE]
> Tài liệu này cụ thể hóa toàn bộ kết quả của [Giai đoạn 1](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/01-requirements/01-STAGE1-DISCOVERY-AND-BRD.md) thành **Đặc tả yêu cầu phần mềm (SRS)** theo đúng mục 5 của [Planning.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/Planning.md#L220-L284), gồm:
> 1. **Functional Requirements (5.1):** 30 yêu cầu chức năng chi tiết (Input, Processing, Output, Priority, Acceptance Criteria).
> 2. **Non-Functional Requirements (5.2):** 10 nhóm yêu cầu phi chức năng kèm tiêu chí đo lường MVP.
> 3. **User Stories (5.3):** Danh sách User Stories theo vai trò kèm Acceptance Criteria.
> 4. **Permission Matrix (5.4):** Ma trận phân quyền chi tiết (View, Create, Update, Delete, Publish/Approve, Lock/Unlock, Override + Data Scope).

---

## 1. FUNCTIONAL REQUIREMENTS (YÊU CẦU CHỨC NĂNG CHI TIẾT)

### 1.1. Phân hệ 1: Authentication & Authorization (`FR-AUTH`)

#### `FR-AUTH-01` — Đăng nhập hệ thống
- **Actor:** Admin, Phòng đào tạo (`ACADEMIC_AFFAIRS`), Giảng viên (`LECTURER`), Sinh viên (`STUDENT`).
- **Priority:** Must Have (M1).
- **Description:** Cho phép người dùng đăng nhập bằng tên đăng nhập (hoặc email) và mật khẩu để nhận phiên xác thực qua `HttpOnly` Cookie.
- **Input:** `usernameOrEmail` (String, bắt buộc), `password` (String, bắt buộc).
- **Processing:**
  1. Kiểm tra định dạng đầu vào (không rỗng, độ dài hợp lệ).
  2. Tìm bản ghi `users` theo `username` hoặc `email` (không phân biệt hoa thường).
  3. Nếu không tồn tại hoặc `BCrypt.matches(password, user.password_hash) == false` → trả lỗi `401 AUTH_INVALID_CREDENTIALS` (không tiết lộ tài khoản hay mật khẩu sai để chống dò tài khoản).
  4. Kiểm tra `user.status`: nếu là `LOCKED` hoặc `INACTIVE` → trả lỗi `403 AUTH_ACCOUNT_LOCKED` (`BR-02`).
  5. Sinh `accessToken` (JWT, hạn 15 phút, chứa `userId`, `roles`, `permissions`) và `refreshToken` (chuỗi ngẫu nhiên 256-bit, hạn 7 ngày, lưu bản băm SHA-256 vào bảng `refresh_tokens`).
  6. Cập nhật `last_login_at` của `users` và đính kèm `accessToken` + `refreshToken` vào `Set-Cookie` (`HttpOnly`, `SameSite=Lax`, `Path=/`).
- **Output:** Thông tin hồ sơ tóm tắt (`userId`, `username`, `fullName`, `roles`, `permissions`, `redirectPath` theo vai trò) + Cookie xác thực.
- **Acceptance Criteria:**
  - **AC-01:** Đăng nhập đúng tài khoản `ACTIVE` trả về `HTTP 200`, đặt cookie `HttpOnly` và điều hướng đúng trang chủ của từng vai trò.
  - **AC-02:** Nhập sai mật khẩu trả về `HTTP 401` với thông báo chung `"Tài khoản hoặc mật khẩu không chính xác"`.
  - **AC-03:** Tài khoản có `status = LOCKED` đăng nhập trả về `HTTP 403` với thông báo `"Tài khoản đã bị khóa"`.

#### `FR-AUTH-02` — Làm mới phiên (Refresh Token Rotation) và Đăng xuất
- **Actor:** Tất cả người dùng đã đăng nhập.
- **Priority:** Must Have (M1).
- **Description:** Tự động cấp mới Access Token khi hết hạn bằng Refresh Token hợp lệ; thu hồi Refresh Token khi đăng xuất.
- **Input:** `refreshToken` từ `HttpOnly` Cookie.
- **Processing:**
  1. **Khi làm mới (`/api/v1/auth/refresh`):** Băm `refreshToken`, tra cứu trong bảng `refresh_tokens`. Nếu không tồn tại, đã hết hạn (`expires_at < NOW()`) hoặc đã bị thu hồi (`revoked_at IS NOT NULL`) → xóa cookie và trả `401`. Kiểm tra lại `user.status == ACTIVE` (`BR-02`). Thu hồi token cũ, phát hành cặp `accessToken` + `refreshToken` mới (Token Rotation).
  2. **Khi đăng xuất (`/api/v1/auth/logout`):** Đánh dấu `revoked_at = NOW()`, `revoked_reason = 'USER_LOGOUT'` cho `refreshToken` hiện tại và xóa cookie.
- **Output:** Cookie mới (khi refresh) hoặc xóa cookie + `HTTP 200` (khi logout).
- **Acceptance Criteria:**
  - **AC-01:** Khi Access Token hết hạn nhưng Refresh Token còn hạn và tài khoản còn `ACTIVE`, gọi `/api/v1/auth/refresh` nhận được Access Token mới.
  - **AC-02:** Sau khi gọi `/api/v1/auth/logout`, Refresh Token cũ không thể dùng để lấy Access Token mới nữa (`HTTP 401`).

#### `FR-AUTH-03` — Đổi mật khẩu cá nhân
- **Actor:** Tất cả người dùng đã đăng nhập.
- **Priority:** Must Have (M1).
- **Description:** Người dùng tự đổi mật khẩu của mình khi biết mật khẩu hiện tại.
- **Input:** `currentPassword`, `newPassword` (tối thiểu 8 ký tự, gồm chữ và số), `confirmPassword`.
- **Processing:**
  1. Kiểm tra `newPassword == confirmPassword` và `newPassword != currentPassword`.
  2. Xác minh `currentPassword` khớp với `user.password_hash`.
  3. Băm `newPassword` bằng `BCrypt` (`BR-01`), cập nhật `users.password_hash`.
  4. Thu hồi toàn bộ `refresh_tokens` cũ của người dùng đó để buộc các thiết bị khác đăng nhập lại.
- **Output:** Thông báo đổi mật khẩu thành công (`HTTP 200`).
- **Acceptance Criteria:**
  - **AC-01:** Nhập sai `currentPassword` trả về `HTTP 400`.
  - **AC-02:** Đổi thành công thì mật khẩu cũ không đăng nhập được nữa, các phiên Refresh Token cũ bị thu hồi.

#### `FR-AUTH-04` — Quên mật khẩu và Đặt lại mật khẩu qua Email
- **Actor:** Người dùng chưa đăng nhập.
- **Priority:** Should Have (M2).
- **Description:** Gửi liên kết/mã đặt lại mật khẩu có thời hạn (15 phút) tới email đăng ký của tài khoản.
- **Input:** `email` (bước 1); `resetToken`, `newPassword` (bước 2).
- **Processing:**
  1. Bước 1: Nếu email tồn tại và tài khoản `ACTIVE`, tạo token đặt lại mật khẩu (lưu hash và thời hạn 15 phút), gửi email qua SMTP/Mailpit. Luôn trả thông báo thành công chung để tránh lộ email tồn tại hay không.
  2. Bước 2: Kiểm tra `resetToken` còn hạn và chưa sử dụng → cập nhật `password_hash`, đánh dấu token đã dùng và thu hồi toàn bộ `refresh_tokens` của user.
- **Output:** Email chứa link đặt lại mật khẩu; mật khẩu mới được cập nhật.
- **Acceptance Criteria:**
  - **AC-01:** Token quá 15 phút hoặc đã dùng 1 lần bị từ chối (`HTTP 400`).

#### `FR-AUTH-05` — Quản lý tài khoản và Khóa/Mở khóa tài khoản
- **Actor:** Admin.
- **Priority:** Must Have (M1).
- **Description:** Admin xem danh sách tài khoản, tạo tài khoản quản trị/đào tạo, khóa hoặc mở khóa tài khoản, đặt lại mật khẩu khi cần.
- **Input:** Bộ lọc tìm kiếm; hoặc `userId`, `newStatus` (`ACTIVE` / `LOCKED`), `reason`.
- **Processing:**
  1. Kiểm tra Admin không tự khóa chính tài khoản của mình.
  2. Cập nhật `users.status`. Nếu chuyển sang `LOCKED`, thu hồi toàn bộ `refresh_tokens` đang hoạt động của `userId` đó (`BR-02`).
  3. Ghi bản ghi vào `audit_logs` (`BR-32`).
- **Output:** Trạng thái tài khoản mới và bản ghi Audit Log.
- **Acceptance Criteria:**
  - **AC-01:** Ngay sau khi Admin khóa tài khoản X, người dùng X không thể đăng nhập hoặc làm mới token.
  - **AC-02:** Admin không thể tự khóa tài khoản đang đăng nhập của chính mình (`HTTP 400`).

#### `FR-AUTH-06` — Quản lý Vai trò (`Role`) và Quyền hạn (`Permission`)
- **Actor:** Admin.
- **Priority:** Must Have (M1/M2).
- **Description:** Xem và phân quyền RBAC giữa `Role` và `Permission`, gán `Role` cho `User`.
- **Input:** `roleId`, danh sách `permissionIds`; hoặc `userId`, danh sách `roleIds`.
- **Processing:** Cập nhật bảng trung gian `role_permissions` hoặc `user_roles` trong Transaction và ghi `audit_logs`.
- **Output:** Cấu hình quyền được cập nhật.
- **Acceptance Criteria:**
  - **AC-01:** Mọi API ở Backend từ chối ngay (`HTTP 403`) nếu Role của User không chứa Permission tương ứng (`BR-03`).

---

### 1.2. Phân hệ 2: Quản lý Khoa, Bộ môn, Ngành và Lớp hành chính (`FR-ORG`)

#### `FR-ORG-01` — Quản lý Khoa (`Faculty`) và Bộ môn (`Department`)
- **Actor:** Phòng đào tạo (`ACADEMIC_AFFAIRS`).
- **Priority:** Must Have (M1).
- **Description:** Tạo, xem, cập nhật, đổi trạng thái (`ACTIVE`/`INACTIVE`) Khoa và Bộ môn trực thuộc Khoa.
- **Input:** Khoa: `code`, `name`, `description`; Bộ môn: `code`, `name`, `facultyId`.
- **Processing:** Kiểm tra trùng `code` (`BR-05`); kiểm tra `facultyId` tồn tại (`BR-06`). Khi ngưng hoạt động hoặc xóa: nếu đã có Bộ môn/Ngành/Giảng viên/Học phần trực thuộc thì chặn xóa vật lý (`BR-07`).
- **Output:** Bản ghi Khoa / Bộ môn sau khi lưu.
- **Acceptance Criteria:**
  - **AC-01:** Tạo trùng mã Khoa hoặc mã Bộ môn trả về `HTTP 409 CONFLICT`.
  - **AC-02:** Không cho phép xóa vật lý Khoa đã có Ngành hoặc Bộ môn trực thuộc.

#### `FR-ORG-02` — Quản lý Ngành đào tạo (`Major`)
- **Actor:** Phòng đào tạo.
- **Priority:** Must Have (M1).
- **Description:** Tạo, xem, sửa, đổi trạng thái Ngành đào tạo thuộc Khoa.
- **Input:** `code` (ví dụ `DCCN`), `name` (Công nghệ thông tin), `facultyId`, `totalCreditsRequired` (ví dụ `150`).
- **Processing:** Kiểm tra trùng `code`, sự tồn tại của `facultyId` và `totalCreditsRequired > 0`.
- **Output:** Thông tin Ngành đào tạo.
- **Acceptance Criteria:**
  - **AC-01:** Mã ngành duy nhất; mỗi ngành bắt buộc thuộc 1 Khoa hợp lệ.

#### `FR-ORG-03` — Quản lý Lớp hành chính (`AcademicClass`)
- **Actor:** Phòng đào tạo.
- **Priority:** Must Have (M1).
- **Description:** Quản lý các lớp quản lý sinh viên theo Ngành và Khóa nhập học (ví dụ `D22CQCN01-B`, khóa `2022`).
- **Input:** `code`, `name`, `majorId`, `cohortYear` (năm nhập học, ví dụ `2022`).
- **Processing:** Kiểm tra trùng `code`, kiểm tra `majorId` hợp lệ (`BR-06`).
- **Output:** Thông tin Lớp hành chính kèm sĩ số sinh viên thuộc lớp.
- **Acceptance Criteria:**
  - **AC-01:** Tạo trùng mã lớp hành chính trả về `HTTP 409`.

#### `FR-ORG-04` — Quản lý hồ sơ Giảng viên (`Lecturer`)
- **Actor:** Phòng đào tạo.
- **Priority:** Must Have (M1).
- **Description:** Tạo mới, cập nhật, tra cứu hồ sơ giảng viên gắn với Bộ môn và tự động khởi tạo tài khoản đăng nhập có Role `LECTURER`.
- **Input:** `lecturerCode` (ví dụ `GV001`), `fullName`, `email`, `departmentId`, `degree` (ThS/TS), `academicRank`, `phone`.
- **Processing:** Trong 1 Transaction: kiểm tra trùng `lecturerCode` và `email`; tạo bản ghi `users` (mật khẩu khởi tạo băm BCrypt, gán Role `LECTURER`) và tạo bản ghi `lecturers` liên kết `user_id` (`BR-04`, `BR-05`).
- **Output:** Hồ sơ giảng viên và tài khoản đăng nhập tương ứng.
- **Acceptance Criteria:**
  - **AC-01:** Tạo giảng viên thành công sẽ đồng thời tạo tài khoản `users` có Role `LECTURER`. Nếu trùng email hoặc mã GV thì rollback cả hai bảng.

---

### 1.3. Phân hệ 3: Quản lý Hồ sơ Sinh viên (`FR-STU`)

#### `FR-STU-01` — Quản lý hồ sơ Sinh viên (Tạo, Xem, Sửa, Lọc)
- **Actor:** Phòng đào tạo.
- **Priority:** Must Have (M1).
- **Description:** Tạo mới hồ sơ sinh viên (tự động tạo tài khoản User gắn Role `STUDENT`), cập nhật thông tin học vụ/lý lịch, tìm kiếm và lọc danh sách sinh viên.
- **Input:** `studentCode` (`B22DCCN001`), `fullName`, `dateOfBirth`, `gender`, `nationalId` (CCCD), `email`, `personalEmail`, `phone`, `address`, `academicClassId`, `majorId`, `cohortYear`.
- **Processing:**
  1. Kiểm tra định dạng và tính duy nhất của `studentCode`, `email`, `nationalId` (`BR-05`).
  2. Kiểm tra `academicClassId` thuộc đúng `majorId`.
  3. Trong 1 Transaction: tạo `users` (Role `STUDENT`, trạng thái `ACTIVE`) → tạo `students` (`academic_status = ACTIVE`) → ghi `student_status_history` khởi tạo.
- **Output:** Hồ sơ sinh viên chi tiết hoặc danh sách phân trang (`Page<StudentDto>`).
- **Acceptance Criteria:**
  - **AC-01:** Tạo trùng `studentCode`, `email` hoặc `nationalId` trả về `HTTP 409` và không tạo tài khoản rác trong bảng `users`.
  - **AC-02:** Hỗ trợ lọc sinh viên theo từ khóa (Mã SV, Họ tên), Khoa, Ngành, Lớp hành chính, Khóa học và Trạng thái học vụ.

#### `FR-STU-02` — Chuyển đổi trạng thái học vụ của Sinh viên
- **Actor:** Phòng đào tạo.
- **Priority:** Must Have (M1).
- **Description:** Thay đổi trạng thái học vụ của sinh viên (`ACTIVE`, `ON_LEAVE`, `SUSPENDED`, `GRADUATED`, `DROPPED_OUT`) và lưu lịch sử biến động.
- **Input:** `studentId`, `newStatus`, `reason` (bắt buộc).
- **Processing:** Kiểm tra `newStatus != oldStatus`; cập nhật `students.academic_status`, thêm bản ghi vào `student_status_history` và `audit_logs` (`BR-08`).
- **Output:** Hồ sơ sinh viên với trạng thái mới và danh sách lịch sử trạng thái.
- **Acceptance Criteria:**
  - **AC-01:** Không nhập `reason` khi đổi trạng thái học vụ trả về `HTTP 400`.
  - **AC-02:** Sinh viên chuyển sang `ON_LEAVE` hoặc `SUSPENDED` lập tức bị chặn khi gọi API đăng ký học phần (`BR-19`).

#### `FR-STU-03` — Sinh viên xem hồ sơ và tự cập nhật thông tin liên lạc
- **Actor:** Sinh viên.
- **Priority:** Must Have (M1).
- **Description:** Sinh viên xem toàn bộ hồ sơ của chính mình và chỉ được sửa 4 trường liên lạc (`phone`, `personalEmail`, `address`, `avatarUrl`).
- **Input:** `phone`, `personalEmail`, `address`, `avatarUrl` (lấy `studentId` từ JWT của chính sinh viên đang đăng nhập).
- **Processing:** Chỉ nhận DTO gồm 4 trường được phép (`BR-09`), kiểm tra định dạng SĐT/Email, cập nhật vào bản ghi `students` tương ứng với `currentUserId`.
- **Output:** Hồ sơ cá nhân sau cập nhật.
- **Acceptance Criteria:**
  - **AC-01:** Sinh viên không thể sửa `fullName`, `studentCode`, `nationalId`, `majorId`, `academicStatus` dù cố tình truyền thêm trường trong JSON payload.
  - **AC-02:** Sinh viên A không thể xem hoặc sửa hồ sơ của Sinh viên B (`HTTP 403` / chỉ truy vấn theo `currentUserId`).

#### `FR-STU-04` — Import danh sách Sinh viên từ CSV/Excel
- **Actor:** Phòng đào tạo.
- **Priority:** Should Have (M2).
- **Description:** Tải lên file `.xlsx` hoặc `.csv` để kiểm tra trước (Preview) và nhập hàng loạt hồ sơ sinh viên theo nguyên tắc All-or-Nothing.
- **Input:** File `.xlsx`/`.csv` theo mẫu.
- **Processing:** Đọc từng dòng, kiểm tra định dạng, kiểm tra trùng lặp ngay trong file và trùng lặp với Database, kiểm tra mã Lớp hành chính / Ngành tồn tại (`BR-32`). Nếu có dòng lỗi → trả danh sách lỗi kèm số dòng. Nếu 100% hợp lệ và người dùng xác nhận → lưu toàn bộ trong 1 Transaction.
- **Output:** Kết quả Preview (số dòng hợp lệ, danh sách lỗi theo dòng) hoặc số lượng sinh viên đã tạo thành công.
- **Acceptance Criteria:**
  - **AC-01:** Nếu dòng thứ 50 trong file 100 dòng bị trùng Mã SV, hệ thống báo rõ lỗi ở dòng 50 và không lưu bất kỳ dòng nào vào DB.

---

### 1.4. Phân hệ 4: Quản lý Học phần & Điều kiện Tiên quyết (`FR-CRS`)

#### `FR-CRS-01` — Quản lý Học phần (`Course`) và Thành phần điểm (`GradeComponent`)
- **Actor:** Phòng đào tạo.
- **Priority:** Must Have (M1).
- **Description:** Tạo, xem, sửa, ngưng hoạt động Học phần kèm cấu hình danh sách thành phần điểm và trọng số của học phần đó.
- **Input:** `code` (`INT1306`), `name`, `credits` (`>= 1`), `theoryHours`, `practiceHours`, `departmentId`, danh sách `gradeComponents` (`code`, `name`, `weightPercent`, `isFinalExam`, `minThreshold`).
- **Processing:**
  1. Kiểm tra `code` duy nhất (`BR-05`), `credits >= 1` (`BR-10`).
  2. Kiểm tra tổng `weightPercent` của tất cả `gradeComponents` của môn học đúng bằng `100` (`BR-28`), và có đúng 1 thành phần cuối kỳ (`isFinalExam = true`, mặc định `minThreshold = 4.0`).
  3. Lưu `courses` và `grade_components` trong 1 Transaction. (Không cho sửa cấu trúc `gradeComponents` của môn nếu trong học kỳ hiện hành đã có lớp học phần bắt đầu nhập điểm).
- **Output:** Thông tin Học phần kèm danh sách thành phần điểm.
- **Acceptance Criteria:**
  - **AC-01:** Tổng trọng số các thành phần điểm bằng `90%` hoặc `110%` sẽ bị từ chối với `HTTP 400`.

#### `FR-CRS-02` — Thiết lập điều kiện Tiên quyết (`PREREQUISITE`) và Học trước (`COREQUISITE`)
- **Actor:** Phòng đào tạo.
- **Priority:** Must Have (M1).
- **Description:** Khai báo danh sách các môn điều kiện của một học phần và kiểm tra chống phụ thuộc vòng.
- **Input:** `courseId`, danh sách `{ prerequisiteCourseId, conditionType: 'PREREQUISITE' | 'COREQUISITE' }`.
- **Processing:**
  1. Kiểm tra `courseId != prerequisiteCourseId`.
  2. Thực hiện thuật toán duyệt đồ thị có hướng (DFS/BFS) trên bảng `course_prerequisites` để đảm bảo việc thêm cạnh mới **không tạo chu trình** (`BR-12`).
  3. Lưu danh sách vào `course_prerequisites`.
- **Output:** Danh sách học phần điều kiện của môn học.
- **Acceptance Criteria:**
  - **AC-01:** Nếu môn A là tiên quyết của môn B, khi thiết lập môn B là tiên quyết của môn A (hoặc qua trung gian C), hệ thống phát hiện chu trình và trả về `HTTP 400`.

---

### 1.5. Phân hệ 5: Quản lý Chương trình đào tạo (`FR-CUR`)

#### `FR-CUR-01` — Quản lý Chương trình đào tạo (`Curriculum`)
- **Actor:** Phòng đào tạo.
- **Priority:** Should Have (M2).
- **Description:** Thiết lập khung chương trình đào tạo cho từng cặp `(majorId, cohortYear)` và danh sách các học phần theo từng học kỳ khuyến nghị.
- **Input:** `code`, `name`, `majorId`, `cohortYear`, danh sách `{ courseId, recommendedSemester, isRequired }`.
- **Processing:** Kiểm tra không trùng cặp `(majorId, cohortYear)` và không trùng `courseId` trong cùng 1 chương trình (`BR-13`); tự động tính tổng số tín chỉ bắt buộc và tự chọn.
- **Output:** Khung chương trình đào tạo chi tiết.
- **Acceptance Criteria:**
  - **AC-01:** Thêm cùng 1 môn học 2 lần vào 1 chương trình đào tạo bị từ chối (`HTTP 400`).

#### `FR-CUR-02` — Xem Chương trình đào tạo & Tiến độ hoàn thành của Sinh viên
- **Actor:** Sinh viên, Phòng đào tạo.
- **Priority:** Should Have (M2).
- **Description:** Hiển thị khung chương trình đào tạo của ngành/khóa mà sinh viên theo học, kèm trạng thái từng môn đối với sinh viên đó (`PASSED` – Đã đạt, `ENROLLED` – Đang học, `FAILED` – Chưa đạt, `NOT_STARTED` – Chưa học) và điểm cao nhất đạt được.
- **Input:** `studentId` (Sinh viên tự xem của mình, hoặc Phòng đào tạo truyền `studentId`).
- **Processing:** Lấy `Curriculum` theo `(student.major_id, student.cohort_year)`, kết nối (LEFT JOIN) với kết quả học tập `enrollments` và điều kiện tiên quyết `course_prerequisites`.
- **Output:** Danh sách học phần phân theo học kỳ khuyến nghị kèm tiến độ tín chỉ tích lũy.
- **Acceptance Criteria:**
  - **AC-01:** Hiển thị chính xác tổng số tín chỉ đã tích lũy / tổng số tín chỉ yêu cầu của chương trình đào tạo.

---

### 1.6. Phân hệ 6: Quản lý Năm học, Học kỳ & Đợt đăng ký (`FR-SEM`)

#### `FR-SEM-01` — Quản lý Năm học (`AcademicYear`) và Học kỳ (`Semester`)
- **Actor:** Phòng đào tạo.
- **Priority:** Must Have (M1).
- **Description:** Tạo và quản lý Năm học, Học kỳ kèm cấu hình giới hạn tín chỉ tối thiểu/tối đa của từng học kỳ.
- **Input:** `code` (`20261`), `name`, `academicYearId`, `termType` (`REGULAR` / `SUMMER`), `startDate`, `endDate`, `minCredits`, `maxCredits`, `status` (`UPCOMING`, `ONGOING`, `CLOSED`).
- **Processing:** Kiểm tra `code` duy nhất, `startDate < endDate`, `0 <= minCredits <= maxCredits` (`BR-23`).
- **Output:** Thông tin Học kỳ.
- **Acceptance Criteria:**
  - **AC-01:** `minCredits > maxCredits` hoặc `startDate >= endDate` bị từ chối (`HTTP 400`).

#### `FR-SEM-02` — Cấu hình và Đóng/Mở Đợt đăng ký học phần
- **Actor:** Phòng đào tạo.
- **Priority:** Must Have (M1).
- **Description:** Thiết lập khoảng thời gian mở đăng ký `[registrationStartAt, registrationEndAt]` và cờ bật/tắt đợt đăng ký `isRegistrationOpen` cho học kỳ.
- **Input:** `semesterId`, `registrationStartAt`, `registrationEndAt`, `isRegistrationOpen`.
- **Processing:** Kiểm tra `registrationStartAt < registrationEndAt`; cập nhật bản ghi `semesters` và ghi `audit_logs`.
- **Output:** Trạng thái đợt đăng ký của học kỳ.
- **Acceptance Criteria:**
  - **AC-01:** Khi `isRegistrationOpen = false` hoặc thời gian hiện tại nằm ngoài `[registrationStartAt, registrationEndAt]`, sinh viên gọi API đăng ký/hủy đều bị từ chối (`BR-20`).

---

### 1.7. Phân hệ 7: Quản lý Lớp học phần & Phòng học (`FR-SEC`)

#### `FR-SEC-01` — Quản lý Phòng học (`Classroom`)
- **Actor:** Phòng đào tạo.
- **Priority:** Must Have (M1).
- **Description:** Tạo, xem, sửa, đổi trạng thái Phòng học và sức chứa (`capacity`).
- **Input:** `code` (`401-A2`), `building` (`A2`), `capacity` (`>= 10`), `roomType` (`THEORY` / `LAB`), `status`.
- **Processing:** Kiểm tra trùng `code`, `capacity > 0`.
- **Output:** Thông tin Phòng học.
- **Acceptance Criteria:**
  - **AC-01:** Không cho phép giảm `capacity` của phòng học xuống nhỏ hơn `max_capacity` của bất kỳ lớp học phần đang mở nào xếp tại phòng đó (`BR-16`).

#### `FR-SEC-02` — Mở Lớp học phần, Phân công Giảng viên & Xếp lịch học
- **Actor:** Phòng đào tạo.
- **Priority:** Must Have (M1).
- **Description:** Tạo/cập nhật Lớp học phần (`CourseSection`) trong học kỳ, gán Giảng viên phụ trách và gán danh sách buổi học (`SectionSchedule`) kèm kiểm tra trùng lịch Phòng học & Giảng viên.
- **Input:** `courseId`, `semesterId`, `groupNumber`, `lecturerId`, `maxCapacity`, danh sách `schedules`: `[{ dayOfWeek, startPeriod, endPeriod, startWeek, endWeek, classroomId }]`.
- **Processing:**
  1. Sinh hoặc kiểm tra mã `sectionCode` duy nhất (ví dụ `INT1306-20261-01`) và cặp `(courseId, semesterId, groupNumber)` duy nhất.
  2. Kiểm tra mỗi buổi học có `1 <= startPeriod <= endPeriod <= 12`, `1 <= startWeek <= endWeek`, và các buổi của chính lớp đó không tự trùng nhau (`BR-14`).
  3. Kiểm tra `classroom.capacity >= maxCapacity` (`BR-16`).
  4. **Kiểm tra trùng lịch Phòng học (`BR-16`):** truy vấn các `section_schedules` của các lớp đang mở trong cùng `semesterId` và cùng `classroomId` xem có buổi nào trùng `dayOfWeek` + giao đoạn tuần + giao đoạn tiết (`BR-15`) không. Nếu có → trả lỗi `409 SCHEDULE_CLASSROOM_CONFLICT` kèm thông tin lớp bị trùng.
  5. **Kiểm tra trùng lịch Giảng viên (`BR-17`):** truy vấn các `section_schedules` của các lớp đang mở trong cùng `semesterId` do cùng `lecturerId` phụ trách xem có trùng theo `BR-15` không. Nếu có → trả lỗi `409 SCHEDULE_LECTURER_CONFLICT` kèm thông tin lớp bị trùng.
  6. Khi cập nhật `maxCapacity`: kiểm tra `maxCapacity >= currentEnrolled` (`BR-18`).
- **Output:** Thông tin Lớp học phần kèm lịch học.
- **Acceptance Criteria:**
  - **AC-01:** Xếp 2 lớp học phần vào cùng phòng `401-A2`, cùng Thứ 2, Tuần 1–15, Lớp 1 học Tiết 1–3 và Lớp 2 học Tiết 3–5 (giao nhau ở Tiết 3) bị chặn với lỗi `409 SCHEDULE_CLASSROOM_CONFLICT`.
  - **AC-02:** Phân công cùng 1 giảng viên dạy 2 lớp trùng Thứ 4, Tiết 7–9, Tuần 5–10 bị chặn với lỗi `409 SCHEDULE_LECTURER_CONFLICT`.
  - **AC-03:** Xếp lớp có `maxCapacity = 80` vào phòng máy chỉ có `capacity = 40` bị chặn với lỗi `400 CLASSROOM_CAPACITY_EXCEEDED`.

#### `FR-SEC-03` — Tra cứu Lớp học phần và Danh sách Sinh viên của lớp
- **Actor:** Phòng đào tạo, Giảng viên, Sinh viên.
- **Priority:** Must Have (M1).
- **Description:** Tra cứu danh sách lớp học phần mở theo học kỳ/môn học; xem danh sách sinh viên đã đăng ký vào một lớp học phần (theo quyền).
- **Input:** Bộ lọc `semesterId`, `courseId`, `facultyId`, `keyword`; hoặc `sectionId`.
- **Processing:**
  - Danh sách lớp mở trong kỳ: cả 4 Actor đều xem được (hiển thị Mã lớp, Môn học, Số TC, Giảng viên, Lịch học, Phòng học, `currentEnrolled / maxCapacity`).
  - Danh sách sinh viên trong lớp (`sectionId`): chỉ Phòng đào tạo, Admin, hoặc **chính Giảng viên được phân công dạy lớp đó** mới xem được (`BR-03`). Sinh viên không xem được danh sách sinh viên chi tiết của các lớp.
- **Output:** Danh sách lớp học phần hoặc danh sách sinh viên thuộc lớp.
- **Acceptance Criteria:**
  - **AC-01:** Giảng viên A truy cập danh sách sinh viên của lớp do Giảng viên B phụ trách nhận `HTTP 403 FORBIDDEN`.

---

### 1.8. Phân hệ 8: Quản lý Đăng ký học phần (`FR-ENR`) — Trọng tâm nghiệp vụ

#### `FR-ENR-01` — Sinh viên tự đăng ký Lớp học phần
- **Actor:** Sinh viên (`STUDENT`).
- **Priority:** Must Have (M1).
- **Description:** Sinh viên đăng ký vào một lớp học phần đang mở trong học kỳ hiện hành; hệ thống kiểm tra toàn bộ 7 quy tắc nghiệp vụ và xử lý tranh chấp đồng thời bằng khóa dòng trên PostgreSQL.
- **Input:** `sectionId` (lấy `studentId` từ thông tin xác thực của sinh viên đang đăng nhập).
- **Processing (thực thi trong 1 `@Transactional` duy nhất):**
  1. **Kiểm tra trạng thái sinh viên (`BR-19`):** Lấy hồ sơ `Student` của user hiện tại. Nếu `academic_status != 'ACTIVE'` → ném lỗi `403 ENROLL_STUDENT_NOT_ACTIVE`.
  2. **Khóa bản ghi Lớp học phần (`BR-25`):** Thực thi `SELECT ... FROM course_sections WHERE id = :sectionId FOR UPDATE` để tuần tự hóa các giao dịch cùng nhắm vào lớp học phần này.
  3. **Kiểm tra trạng thái lớp & đợt đăng ký (`BR-20`):** Kiểm tra `section.status == 'OPEN'`, học kỳ `semester.is_registration_open == true` và `NOW()` nằm trong `[registration_start_at, registration_end_at]`. Nếu không → ném lỗi `400 ENROLL_PERIOD_CLOSED` hoặc `400 ENROLL_SECTION_CLOSED`.
  4. **Kiểm tra sĩ số lớp (`BR-25`):** Kiểm tra `section.current_enrolled < section.max_capacity`. Nếu đã đầy → ném lỗi `409 ENROLL_SECTION_FULL`.
  5. **Kiểm tra trùng học phần trong cùng học kỳ (`BR-21`):** Kiểm tra sinh viên chưa có bản ghi `enrollments` nào với `(student_id, course_id = section.course_id, semester_id = section.semester_id, status = 'ENROLLED')`. Nếu đã có → ném lỗi `409 ENROLL_DUPLICATE_COURSE_IN_SEMESTER`.
  6. **Kiểm tra điều kiện Tiên quyết & Học trước (`BR-22`):** Lấy danh sách `course_prerequisites` của `section.course_id`:
     - Với mỗi môn `PREREQUISITE`: kiểm tra sinh viên đã có kết quả ở học kỳ trước với `is_passed = true` (điểm ≥ D).
     - Với mỗi môn `COREQUISITE`: kiểm tra sinh viên đã từng học ở học kỳ trước và đã có điểm tổng kết (`final_score_10 IS NOT NULL`).
     - Nếu thiếu → ném lỗi `400 ENROLL_PREREQUISITE_NOT_MET` kèm mã và tên môn điều kiện còn thiếu.
  7. **Kiểm tra giới hạn tín chỉ tối đa của học kỳ (`BR-23`):** Tính tổng `credits` của các môn đang `ENROLLED` trong học kỳ + `course.credits`. Nếu `> semester.max_credits` → ném lỗi `400 ENROLL_MAX_CREDITS_EXCEEDED` kèm số tín chỉ hiện tại và giới hạn tối đa.
  8. **Kiểm tra trùng lịch học (`BR-24`):** So sánh các buổi học `section_schedules` của `sectionId` với toàn bộ buổi học của các lớp mà sinh viên đang `ENROLLED` trong cùng học kỳ theo công thức giao đoạn `BR-15`. Nếu trùng → ném lỗi `409 ENROLL_SCHEDULE_CONFLICT` kèm thông tin lớp và tiết học bị trùng.
  9. **Ghi nhận đăng ký:** Tính `attempt_number` (lần học thứ mấy của môn này đối với sinh viên), tạo/cập nhật bản ghi `enrollments` với `status = 'ENROLLED'`, tăng `course_sections.current_enrolled = current_enrolled + 1`, ghi `audit_logs` → Commit Transaction.
- **Output:** Bản ghi kết quả đăng ký thành công kèm tổng số tín chỉ hiện tại trong kỳ.
- **Acceptance Criteria:**
  - **AC-01:** Vi phạm bất kỳ điều kiện nào trong các bước 1 → 8 đều rollback transaction, giữ nguyên `current_enrolled` và trả mã lỗi nghiệp vụ cụ thể.
  - **AC-02 (Kiểm thử đồng thời):** Khi lớp còn đúng 1 chỗ trống (`49/50`) và có 10 request đăng ký hợp lệ gửi đến đồng thời, đúng **01** request nhận `HTTP 201 CREATED`, **09** request còn lại nhận `HTTP 409 ENROLL_SECTION_FULL`, và `current_enrolled` sau cùng đúng bằng `50`.

#### `FR-ENR-02` — Sinh viên tự hủy đăng ký Lớp học phần
- **Actor:** Sinh viên (`STUDENT`).
- **Priority:** Must Have (M1).
- **Description:** Sinh viên hủy một lớp học phần đã đăng ký khi đợt đăng ký của học kỳ đang mở.
- **Input:** `enrollmentId` (hoặc `sectionId`).
- **Processing (trong 1 `@Transactional`):**
  1. Kiểm tra bản ghi `enrollment` thuộc về chính sinh viên đang đăng nhập (`BR-03`) và có `status == 'ENROLLED'`.
  2. Kiểm tra đợt đăng ký của học kỳ đang mở (`BR-20`) và lớp học phần chưa phát sinh điểm công bố.
  3. Khóa bản ghi `course_sections` (`FOR UPDATE`), chuyển `enrollment.status = 'CANCELLED'`, cập nhật `cancelled_at = NOW()`, `cancelled_by = currentUserId`, giảm `course_sections.current_enrolled = current_enrolled - 1` (`BR-26`).
- **Output:** Thông báo hủy đăng ký thành công, hoàn lại 1 suất trống cho lớp.
- **Acceptance Criteria:**
  - **AC-01:** Sau khi đợt đăng ký đã đóng, sinh viên gọi API hủy nhận `HTTP 400 ENROLL_PERIOD_CLOSED`.
  - **AC-02:** Hủy thành công không xóa dòng dữ liệu trong bảng `enrollments` mà đổi `status = 'CANCELLED'` và giảm `current_enrolled` đi 1.

#### `FR-ENR-03` — Phòng đào tạo đăng ký / hủy đăng ký hộ Sinh viên
- **Actor:** Phòng đào tạo (`ACADEMIC_AFFAIRS`).
- **Priority:** Must Have (M1).
- **Description:** Cho phép cán bộ Phòng đào tạo thêm hoặc hủy học phần cho một sinh viên cụ thể (kể cả ngoài đợt đăng ký tự động), bắt buộc nhập lý do và ghi Audit Log.
- **Input:** `studentId`, `sectionId`, `action` (`ENROLL` / `CANCEL`), `reason` (bắt buộc).
- **Processing:** Thực hiện các kiểm tra toàn vẹn cốt lõi (sĩ số tối đa `FOR UPDATE`, chống trùng môn trong kỳ, trùng lịch học, trạng thái bảng điểm chưa khóa), ghi `enrolled_by` / `cancelled_by` là user của Phòng đào tạo và ghi bản ghi vào `audit_logs` (`BR-20`, `BR-32`).
- **Output:** Kết quả đăng ký/hủy hộ và bản ghi Audit Log.
- **Acceptance Criteria:**
  - **AC-01:** Không truyền `reason` khi đăng ký/hủy hộ trả về `HTTP 400`.

---

### 1.9. Phân hệ 9: Quản lý Thời khóa biểu (`FR-SCH`)

#### `FR-SCH-01` — Sinh viên xem Thời khóa biểu cá nhân
- **Actor:** Sinh viên.
- **Priority:** Must Have (M1: dữ liệu & danh sách) / Should Have (M2: giao diện lưới 12 tiết × Thứ 2–7).
- **Description:** Sinh viên xem thời khóa biểu các lớp học phần đang `ENROLLED` theo Học kỳ và lọc theo Tuần học.
- **Input:** `semesterId`, `weekNumber` (tùy chọn, mặc định tất cả các tuần).
- **Processing:** Truy vấn toàn bộ `enrollments` (`status = 'ENROLLED'`) của sinh viên trong `semesterId` kèm `course_sections`, `courses`, `lecturers`, `section_schedules` và `classrooms`. Nếu có `weekNumber`, chỉ lọc các buổi có `start_week <= weekNumber <= end_week`.
- **Output:** Danh sách các ca học để hiển thị trên lưới Thời khóa biểu (Thứ 2 → Thứ 7, Tiết 1 → Tiết 12).
- **Acceptance Criteria:**
  - **AC-01:** Ngay sau khi đăng ký hoặc hủy một lớp học phần, Thời khóa biểu của sinh viên phản ánh đúng thay đổi đó.

#### `FR-SCH-02` — Giảng viên xem Thời khóa biểu giảng dạy
- **Actor:** Giảng viên.
- **Priority:** Must Have (M1/M2).
- **Description:** Giảng viên xem lịch giảng dạy của các lớp học phần được phân công trong Học kỳ / Tuần.
- **Input:** `semesterId`, `weekNumber` (tùy chọn).
- **Processing:** Truy vấn các `course_sections` (`status != 'CANCELLED'`) có `lecturer_id` của giảng viên đang đăng nhập kèm `section_schedules` và `classrooms`.
- **Output:** Lịch giảng dạy theo thứ/tiết/tuần/phòng học.
- **Acceptance Criteria:**
  - **AC-01:** Hiển thị chính xác tất cả các lớp học phần mà giảng viên được phân công trong học kỳ.

---

### 1.10. Phân hệ 10: Quản lý Điểm và Kết quả học tập (`FR-GRD`)

#### `FR-GRD-01` — Giảng viên nhập và cập nhật điểm thành phần
- **Actor:** Giảng viên (`LECTURER`).
- **Priority:** Must Have (M1).
- **Description:** Giảng viên nhập hoặc cập nhật điểm các thành phần điểm (`GradeComponent`) cho danh sách sinh viên đang `ENROLLED` trong lớp học phần mình phụ trách.
- **Input:** `sectionId`, danh sách `[{ enrollmentId, scores: [{ gradeComponentId, score }] }]`.
- **Processing (trong 1 `@Transactional`):**
  1. **Kiểm tra quyền phụ trách (`BR-03`):** Kiểm tra `section.lecturer_id` khớp với giảng viên đang đăng nhập. Nếu không → trả `403 GRADE_NOT_ASSIGNED_LECTURER`.
  2. **Kiểm tra trạng thái bảng điểm (`BR-30`):** Kiểm tra `section.grade_status == 'DRAFT'`. Nếu đã `PUBLISHED` hoặc `LOCKED` → trả `400 GRADE_ALREADY_PUBLISHED_OR_LOCKED`.
  3. **Kiểm tra miền giá trị (`BR-28`):** Mỗi `score` (nếu khác `null`) phải thuộc đoạn `[0.0, 10.0]` và làm tròn 1 chữ số thập phân.
  4. Lưu vào bảng `student_grades` và gọi hàm tính toán điểm tổng kết (`FR-GRD-03`) cho các sinh viên đã có đủ đầu điểm.
- **Output:** Bảng điểm cập nhật của lớp học phần.
- **Acceptance Criteria:**
  - **AC-01:** Giảng viên nhập điểm `10.5` hoặc `-1.0` bị chặn với `HTTP 400`.
  - **AC-02:** Giảng viên A gửi request nhập điểm cho lớp của Giảng viên B nhận `HTTP 403`.
  - **AC-03:** Khi lớp đã chuyển sang `PUBLISHED`, Giảng viên gọi API sửa điểm nhận `HTTP 400`.

#### `FR-GRD-02` — Import bảng điểm Lớp học phần từ CSV/Excel
- **Actor:** Giảng viên.
- **Priority:** Should Have (M2).
- **Description:** Giảng viên tải file mẫu danh sách sinh viên của lớp, điền điểm các thành phần và upload lại để nhập hàng loạt theo nguyên tắc Validate & Preview + All-or-Nothing.
- **Input:** `sectionId`, file `.xlsx`/`.csv`.
- **Processing:** Kiểm tra quyền phụ trách lớp và trạng thái `DRAFT`; kiểm tra tất cả Mã SV trong file đều thuộc lớp học phần đó và mọi đầu điểm thuộc `[0.0, 10.0]` (`BR-32`). Nếu có dòng lỗi → trả danh sách lỗi chi tiết; nếu hợp lệ hoàn toàn → lưu toàn bộ trong 1 Transaction.
- **Output:** Kết quả Preview hoặc bảng điểm sau khi import.
- **Acceptance Criteria:**
  - **AC-01:** Có 1 sinh viên trong file có điểm ngoài `[0, 10]` hoặc không thuộc lớp học phần thì toàn bộ file không được lưu và báo lỗi đúng dòng đó.

#### `FR-GRD-03` — Tự động tính điểm tổng kết hệ 10, điểm chữ, điểm hệ 4 và xét qua môn
- **Actor:** Hệ thống (kích hoạt tự động khi lưu điểm).
- **Priority:** Must Have (M1).
- **Description:** Tính kết quả học phần cho từng sinh viên khi đã nhập đủ các thành phần điểm của môn học.
- **Input:** Danh sách điểm thành phần `student_grades` của một `enrollment` và cấu hình `grade_components` + `grading_scales`.
- **Processing (`BR-28`, `BR-29`):**
  1. Nếu sinh viên đã có đủ điểm của tất cả `grade_components` thuộc môn học:
     $$\text{rawScore10} = \sum_{i=1}^{k} \frac{\text{score}_i \times \text{weightPercent}_i}{100}$$
  2. Làm tròn `rawScore10` đến 1 chữ số thập phân (`HALF_UP`) → `final_score_10`.
  3. Kiểm tra thành phần có `is_final_exam = true`: nếu `score < min_threshold` (mặc định `< 4.0`, điểm liệt cuối kỳ) → gán `letter_grade = 'F'`, `gpa_score_4 = 0.0`, `is_passed = false`.
  4. Ngược lại, tra bảng `grading_scales` theo `final_score_10` để lấy `letter_grade`, `gpa_score_4` và `is_passed`.
- **Output:** Cập nhật `final_score_10`, `letter_grade`, `gpa_score_4`, `is_passed` trên bản ghi `enrollments`.
- **Acceptance Criteria:**
  - **AC-01:** Sinh viên có Chuyên cần `10.0` (10%), KT `10.0` (20%), Giữa kỳ `10.0` (20%), Cuối kỳ `3.5` (50%) → Trung bình trọng số = `6.8`, nhưng do Cuối kỳ `3.5 < 4.0` (điểm liệt) nên kết quả nhận `letter_grade = 'F'`, `gpa_score_4 = 0.0`, `is_passed = false`.
  - **AC-02:** Sinh viên có Chuyên cần `9.0` (10%), KT `8.0` (20%), Giữa kỳ `8.5` (20%), Cuối kỳ `8.5` (50%) → `final_score_10 = 8.5` → `letter_grade = 'A'`, `gpa_score_4 = 4.0`, `is_passed = true`.

#### `FR-GRD-04` — Công bố điểm (`PUBLISHED`), Khóa sổ điểm (`LOCKED`) và Sửa điểm sau công bố
- **Actor:** Giảng viên (Công bố), Phòng đào tạo (Công bố, Khóa sổ, Mở khóa / Sửa điểm đặc biệt).
- **Priority:** Must Have (M1/M2).
- **Description:** Quản lý vòng đời trạng thái bảng điểm của lớp học phần (`DRAFT` → `PUBLISHED` → `LOCKED`) và kiểm soát chặt việc chỉnh sửa sau khi công bố.
- **Input:** `sectionId`, hành động (`PUBLISH` / `LOCK` / `OVERRIDE_SCORE`), `reason` (bắt buộc khi PĐT sửa điểm sau công bố/khóa).
- **Processing (`BR-30`, `BR-32`):**
  1. Khi chuyển `DRAFT` → `PUBLISHED`: kiểm tra 100% sinh viên `ENROLLED` trong lớp đã có đủ các đầu điểm thành phần. Cập nhật `grade_status = 'PUBLISHED'`, `published_at = NOW()`.
  2. Khi chuyển `PUBLISHED` → `LOCKED` (chỉ Phòng đào tạo): cập nhật `grade_status = 'LOCKED'`, `locked_at = NOW()`.
  3. Khi Phòng đào tạo sửa điểm của một sinh viên ở lớp đã `PUBLISHED`/`LOCKED`: bắt buộc có `reason`, tính lại kết quả học phần và ghi chi tiết điểm cũ / điểm mới / lý do vào `audit_logs`.
- **Output:** Trạng thái bảng điểm mới và bản ghi Audit Log.
- **Acceptance Criteria:**
  - **AC-01:** Lớp còn sinh viên chưa nhập đủ đầu điểm thì không thể chuyển sang `PUBLISHED` (`HTTP 400`).
  - **AC-02:** Sinh viên chỉ nhìn thấy điểm của các lớp có `grade_status IN ('PUBLISHED', 'LOCKED')`; các lớp `DRAFT` hiển thị `"Chưa công bố"`.

#### `FR-GRD-05` — Tra cứu kết quả học tập, GPA học kỳ và CPA tích lũy
- **Actor:** Sinh viên (xem của chính mình), Phòng đào tạo (xem của bất kỳ sinh viên nào).
- **Priority:** Must Have (M1/M2).
- **Description:** Xem bảng điểm theo từng học kỳ và toàn khóa, tự động tính GPA học kỳ, CPA tích lũy và tổng số tín chỉ tích lũy.
- **Input:** `studentId` (Sinh viên lấy từ JWT).
- **Processing (`BR-31`):**
  1. Lấy toàn bộ bản ghi `enrollments` (`status = 'ENROLLED'`) của sinh viên ở các lớp có `grade_status IN ('PUBLISHED', 'LOCKED')`.
  2. **GPA học kỳ $s$:**
     $$\text{GPA}_s = \text{round}_2\left(\frac{\sum_{c \in E_s} (\text{gpa\_score\_4}_c \times \text{credits}_c)}{\sum_{c \in E_s} \text{credits}_c}\right)$$
  3. **CPA tích lũy (xử lý học lại / cải thiện theo `D-10`, `BR-31`):** Nhóm các lần học theo `course_id`, với mỗi `course_id` chọn bản ghi có `gpa_score_4` cao nhất (nếu bằng nhau chọn `final_score_10` cao nhất) làm kết quả đại diện $R^*$:
     $$\text{CPA} = \text{round}_2\left(\frac{\sum_{c \in R^*} (\text{gpa\_score\_4}_c \times \text{credits}_c)}{\sum_{c \in R^*} \text{credits}_c}\right)$$
  4. **Tổng tín chỉ tích lũy:** Tổng `credits` của các môn trong $R^*$ có `is_passed = true`.
- **Output:** Bảng điểm chi tiết từng học kỳ, GPA từng kỳ, CPA tích lũy và tổng số tín chỉ tích lũy.
- **Acceptance Criteria:**
  - **AC-01:** Nếu sinh viên học môn Giải tích 1 (3 tín chỉ) ở kỳ 1 bị điểm `F (0.0)` và học lại ở kỳ 2 đạt điểm `B (3.0)`, thì GPA kỳ 1 vẫn tính điểm `F (0.0)`, GPA kỳ 2 tính điểm `B (3.0)`, nhưng **CPA tích lũy** chỉ tính lần học đạt `B (3.0)` và tổng tín chỉ tích lũy cộng đúng 3 tín chỉ (không cộng trùng).

---

### 1.11. Phân hệ 13: Báo cáo, Cấu hình quy chế & Nhật ký kiểm toán (`FR-RPT`, `FR-SYS`)

#### `FR-RPT-01` — Xem Báo cáo và Thống kê đào tạo
- **Actor:** Phòng đào tạo, Admin, Giảng viên (phạm vi lớp mình dạy).
- **Priority:** Should Have (M2).
- **Description:** Cung cấp 4 báo cáo thống kê học vụ.
- **Input:** `semesterId`, `facultyId`, `majorId`, `cohortYear`, `sectionId`.
- **Processing:** Thực thi các câu lệnh SQL tổng hợp (`GROUP BY`, `COUNT`, `AVG`, `CASE WHEN`) trên PostgreSQL.
- **Output:** (1) Quân số sinh viên theo Khoa/Ngành/Trạng thái; (2) Tỷ lệ lấp đầy sĩ số các lớp học phần trong kỳ; (3) Phổ điểm A/B+/B/C+/C/D+/D/F và tỷ lệ qua môn; (4) Phân bố xếp loại học lực theo GPA học kỳ.
- **Acceptance Criteria:**
  - **AC-01:** Số liệu thống kê khớp 100% với dữ liệu thực tế trong các bảng `students`, `enrollments`, `course_sections`.

#### `FR-SYS-01` — Cấu hình Thang điểm quy đổi và Tham số quy chế
- **Actor:** Admin, Phòng đào tạo.
- **Priority:** Should Have (M2).
- **Description:** Xem và cập nhật bảng thang điểm `grading_scales` và bảng cấu hình `system_configs` (quy tắc tính điểm học lại `RETAAKE_POLICY = HIGHEST_SCORE | LATEST_SCORE`, điểm liệt mặc định).
- **Input:** Danh sách các mức điểm trong `grading_scales` hoặc cặp `(configKey, configValue)`.
- **Processing:** Kiểm tra các khoảng điểm trong `grading_scales` phủ kín đoạn `[0.0, 10.0]` và không chồng lấn nhau; cập nhật DB và ghi `audit_logs`.
- **Output:** Cấu hình mới có hiệu lực.
- **Acceptance Criteria:**
  - **AC-01:** Cấu hình khoảng điểm bị chồng lấn (ví dụ `7.0–8.0` và `7.5–8.5`) bị từ chối (`HTTP 400`).

#### `FR-SYS-02` — Tra cứu Nhật ký kiểm toán (`AuditLog`)
- **Actor:** Admin.
- **Priority:** Should Have (M2).
- **Description:** Tra cứu lịch sử các thao tác nhạy cảm trên hệ thống theo người thực hiện, loại hành động, đối tượng bị tác động và khoảng thời gian.
- **Input:** `actorUserId`, `action`, `entityType`, `fromDate`, `toDate`, `page`, `size`.
- **Processing:** Truy vấn phân trang trên bảng `audit_logs` (chỉ đọc, không ai có quyền sửa/xóa bản ghi `audit_logs`).
- **Output:** Danh sách `AuditLog` kèm dữ liệu trước và sau thay đổi (`old_value_json`, `new_value_json`, `reason`).
- **Acceptance Criteria:**
  - **AC-01:** Không tồn tại bất kỳ API nào cho phép sửa hoặc xóa bản ghi trong `audit_logs`.

---

## 2. NON-FUNCTIONAL REQUIREMENTS (YÊU CẦU PHI CHỨC NĂNG — MỤC 5.2)

> [!NOTE]
> Theo yêu cầu mục 5.2 của `Planning.md`, bảng dưới đây phân biệt rõ **Tiêu chí mục tiêu (Target Criteria)** thiết kế cho quy mô MVP và **Cách kiểm chứng (Verification Method)** sẽ thực hiện ở Giai đoạn 8 để chuyển thành tiêu chí đã kiểm chứng.

| Nhóm NFR | Mã | Yêu cầu cụ thể | Tiêu chí mục tiêu (Target) | Cách kiểm chứng ở GĐ8 (Verification) |
|---|---|---|---|---|
| **1. Performance** | `NFR-PERF-01` | Thời gian phản hồi các API CRUD và tra cứu thông thường (trên tập dữ liệu mẫu ~2.000 SV, ~200 lớp học phần, ~20.000 lượt đăng ký) | `p95 < 300ms`, `p99 < 800ms` khi chạy trên môi trường Docker local | Đo bằng script kiểm thử tải (k6 / JMeter / JUnit benchmark) và `EXPLAIN ANALYZE` các câu SQL chính |
| | `NFR-PERF-02` | Xử lý đồng thời khi nhiều sinh viên cùng đăng ký 1 lớp học phần | Chịu được tối thiểu `50 concurrent requests` vào cùng 1 lớp học phần với thời gian phản hồi `< 1.5s` và `0%` lỗi 500 | Integration test đa luồng (`ExecutorService` + `CountDownLatch`) trên PostgreSQL Testcontainers |
| **2. Security** | `NFR-SEC-01` | Lưu trữ mật khẩu và quản lý phiên | Mật khẩu băm `BCrypt` (strength `>= 10`); JWT Access Token (15 phút) + Refresh Token (7 ngày) trong `HttpOnly` Cookie; không lưu token ở `localStorage` | Kiểm tra DB (`password_hash` bắt đầu bằng `$2a$`/`$2b$`), kiểm tra Header `Set-Cookie` trong Security Test |
| | `NFR-SEC-02` | Kiểm soát truy cập Backend (RBAC + Data Scope) | 100% endpoint riêng tư có `@PreAuthorize` và kiểm tra quyền sở hữu dữ liệu; chống IDOR, SQL Injection, XSS, CSRF | Bộ Test Case bảo mật bắt buộc cho cả 4 vai trò (xem mục 4 & Giai đoạn 8) |
| **3. Data Integrity** | `NFR-INT-01` | Toàn vẹn giao dịch và chống vượt sĩ số (No Overbooking) | Tuyệt đối không bao giờ xảy ra `current_enrolled > max_capacity` hoặc 1 SV đăng ký 2 lớp của cùng 1 môn trong 1 kỳ | Ràng buộc `CHECK (current_enrolled <= max_capacity)` + `SELECT ... FOR UPDATE` + Partial Unique Index trong PostgreSQL |
| **4. Reliability** | `NFR-REL-01` | Tính nguyên tử (Atomicity) khi lỗi | Mọi thao tác ghi nhiều bảng (tạo SV + User, đăng ký học phần, import Excel) nếu lỗi ở bước bất kỳ phải rollback 100% | Kiểm thử chèn lỗi giả lập (Fault Injection Test) trong các `@Transactional` service |
| **5. Availability** | `NFR-AVL-01` | Khả năng sẵn sàng và khởi động môi trường demo | Toàn bộ hệ thống (PostgreSQL + Backend + Frontend) khởi động sạch bằng `docker compose up` kèm healthcheck | Kiểm tra health endpoint `/actuator/health` trả về `UP` |
| **6. Scalability** | `NFR-SCA-01` | Khả năng mở rộng kiến trúc | Backend stateless (ngoại trừ bảng `refresh_tokens` trong DB), phân trang (`Pageable`) trên toàn bộ API danh sách (tối đa `100` bản ghi/trang) | Kiểm tra không dùng `HttpSession` lưu trong RAM của JVM; mọi API danh sách đều bắt buộc phân trang |
| **7. Maintainability** | `NFR-MNT-01` | Tổ chức mã nguồn và quản lý schema | Kiến trúc Modular Monolith chia rõ theo module nghiệp vụ; 100% thay đổi DB quản lý qua Flyway migration (`V1__...sql`); chuẩn hóa format lỗi trả về (`ProblemDetail` / `ApiErrorResponse`) | Kiểm tra cấu trúc package và chạy tự động Flyway trên DB trống khi test |
| **8. Usability** | `NFR-USA-01` | Trải nghiệm người dùng và thông báo lỗi | Giao diện Tiếng Việt rõ ràng; mọi lỗi nghiệp vụ trả về thông điệp dễ hiểu cho người dùng cuối; màn hình Sinh viên hiển thị tốt trên cả Desktop (`>= 1280px`) và Mobile (`375px`) | Kiểm thử UI trên các kích thước màn hình chuẩn |
| **9. Backup & Recovery** | `NFR-BAK-01` | Sao lưu và khôi phục dữ liệu | Có script `pg_dump` / `pg_restore` và bộ dữ liệu mẫu tự động nạp lại được bất kỳ lúc nào | Chạy thử quy trình backup & restore trên container PostgreSQL |
| **10. Logging & Audit** | `NFR-LOG-01` | Nhật ký ứng dụng và nhật ký kiểm toán | Log ứng dụng có `requestId`/trace, không lộ mật khẩu/token; các thao tác nhạy cảm ghi đầy đủ vào bảng `audit_logs` | Kiểm tra log console và truy vấn bảng `audit_logs` sau các thao tác khóa TK / đăng ký hộ / sửa điểm |

---

## 3. USER STORIES (MỤC 5.3)

### 3.1. Nhóm Admin
- **`US-ADM-01`:** *As an* **Admin**, *I want to* khóa hoặc mở khóa tài khoản người dùng và thu hồi ngay phiên đăng nhập của họ, *so that* tài khoản vi phạm hoặc nghỉ việc không thể tiếp tục truy cập hệ thống.
  - *Acceptance Criteria:* Khóa tài khoản chuyển `status = LOCKED`, thu hồi toàn bộ Refresh Token của user đó và ghi `AuditLog`.
- **`US-ADM-02`:** *As an* **Admin**, *I want to* quản lý danh sách Vai trò (`Role`) và Quyền hạn (`Permission`), *so that* tôi có thể kiểm soát quyền truy cập chức năng của từng nhóm người dùng mà không phải sửa code.
  - *Acceptance Criteria:* Gán/gỡ Permission khỏi Role có hiệu lực kiểm tra trên các API tương ứng.
- **`US-ADM-03`:** *As an* **Admin**, *I want to* tra cứu Nhật ký kiểm toán (`AuditLog`) theo người thực hiện, hành động và thời gian, *so that* tôi có thể truy vết mọi thao tác nhạy cảm như sửa điểm sau công bố hoặc đăng ký học phần hộ.
  - *Acceptance Criteria:* Hiển thị rõ giá trị cũ, giá trị mới, người thực hiện, lý do và thời gian; không ai xóa/sửa được log.

### 3.2. Nhóm Phòng Quản lý Đào tạo (`ACADEMIC_AFFAIRS`)
- **`US-AA-01`:** *As a* **Cán bộ Phòng đào tạo**, *I want to* quản lý danh mục Khoa, Bộ môn, Ngành, Lớp hành chính và Giảng viên, *so that* hệ thống có cấu trúc tổ chức chuẩn trước khi tiếp nhận sinh viên và mở lớp.
  - *Acceptance Criteria:* Không cho phép trùng mã định danh; không cho xóa vật lý danh mục đã có dữ liệu phụ thuộc.
- **`US-AA-02`:** *As a* **Cán bộ Phòng đào tạo**, *I want to* tạo mới hồ sơ sinh viên (từng người hoặc import từ file Excel) và tự động sinh tài khoản đăng nhập cho sinh viên, *so that* sinh viên có thể sử dụng hệ thống ngay khi nhập học.
  - *Acceptance Criteria:* Tạo sinh viên tự động tạo `User` gắn Role `STUDENT`; import Excel kiểm tra lỗi trước và áp dụng All-or-Nothing.
- **`US-AA-03`:** *As a* **Cán bộ Phòng đào tạo**, *I want to* thiết lập Học phần kèm trọng số thành phần điểm và điều kiện Tiên quyết / Học trước, *so that* hệ thống tự động kiểm tra điều kiện học vụ và tính điểm chính xác.
  - *Acceptance Criteria:* Tổng trọng số thành phần điểm bằng 100%; chặn thiết lập điều kiện tiên quyết tạo vòng lặp.
- **`US-AA-04`:** *As a* **Cán bộ Phòng đào tạo**, *I want to* mở Lớp học phần, phân công Giảng viên và xếp Phòng học + Lịch học có kiểm tra xung đột tự động, *so that* không bao giờ xảy ra tình trạng 2 lớp trùng phòng hoặc 1 giảng viên dạy 2 lớp cùng giờ.
  - *Acceptance Criteria:* Báo lỗi cụ thể nếu trùng lịch Phòng học, trùng lịch Giảng viên hoặc sĩ số lớp vượt sức chứa phòng.
- **`US-AA-05`:** *As a* **Cán bộ Phòng đào tạo**, *I want to* cấu hình giới hạn tín chỉ và đóng/mở đợt đăng ký học phần cho từng học kỳ, *so that* sinh viên chỉ đăng ký được trong khung thời gian và định mức tín chỉ cho phép.
  - *Acceptance Criteria:* Ngoài khoảng thời gian mở đợt đăng ký, sinh viên không thể tự đăng ký hoặc tự hủy.
- **`US-AA-06`:** *As a* **Cán bộ Phòng đào tạo**, *I want to* đăng ký hoặc hủy học phần hộ sinh viên trong các trường hợp đặc biệt (có ghi lý do), *so that* tôi xử lý được các đơn xin học vụ hợp lệ sau khi đợt đăng ký đã đóng.
  - *Acceptance Criteria:* Bắt buộc nhập lý do; vẫn kiểm tra sĩ số và trùng lịch; ghi vết vào `AuditLog`.
- **`US-AA-07`:** *As a* **Cán bộ Phòng đào tạo**, *I want to* khóa sổ điểm cuối kỳ (`LOCKED`) và chỉ cho phép chỉnh sửa điểm sau khi khóa kèm lý do kiểm toán, *so that* dữ liệu điểm toàn trường được bảo toàn.
  - *Acceptance Criteria:* Khi đã `LOCKED`, không ai sửa được điểm qua luồng thông thường; nếu PĐT điều chỉnh đặc biệt thì phải nhập lý do và lưu điểm cũ/mới vào `AuditLog`.

### 3.3. Nhóm Giảng viên (`LECTURER`)
- **`US-LEC-01`:** *As a* **Giảng viên**, *I want to* xem Thời khóa biểu giảng dạy và danh sách sinh viên của các lớp học phần tôi được phân công trong học kỳ, *so that* tôi chủ động tổ chức giảng dạy và quản lý lớp.
  - *Acceptance Criteria:* Chỉ xem được danh sách sinh viên của lớp mình dạy; truy cập lớp của giảng viên khác nhận `HTTP 403`.
- **`US-LEC-02`:** *As a* **Giảng viên**, *I want to* nhập điểm thành phần (trực tiếp trên bảng web hoặc import từ file Excel) khi bảng điểm ở trạng thái `DRAFT`, *so that* hệ thống tự động tính điểm tổng kết hệ 10, điểm chữ và điểm hệ 4 chính xác.
  - *Acceptance Criteria:* Chỉ nhận điểm trong `[0.0, 10.0]`; tự động tính điểm tổng kết và xét điểm liệt cuối kỳ `< 4.0`.
- **`US-LEC-03`:** *As a* **Giảng viên**, *I want to* công bố bảng điểm (`PUBLISHED`) sau khi đã nhập đủ điểm cho cả lớp, *so that* sinh viên xem được kết quả học tập và bảng điểm được bảo vệ khỏi việc sửa nhầm.
  - *Acceptance Criteria:* Chỉ công bố được khi 100% sinh viên trong lớp đã có đủ đầu điểm; sau khi công bố, giảng viên không tự sửa điểm được nữa.

### 3.4. Nhóm Sinh viên (`STUDENT`)
- **`US-STU-01`:** *As a* **Sinh viên**, *I want to* tra cứu các lớp học phần mở trong kỳ và tự đăng ký / hủy đăng ký trong đợt mở, *so that* tôi chủ động xây dựng kế hoạch học tập mỗi kỳ.
  - *Acceptance Criteria:* Hệ thống kiểm tra tức thì trạng thái `ACTIVE`, đợt đăng ký, sĩ số, môn trùng trong kỳ, môn tiên quyết/học trước, giới hạn tín chỉ tối đa và trùng lịch; thông báo rõ nguyên nhân nếu bị chặn.
- **`US-STU-02`:** *As a* **Sinh viên**, *I want to* xem Thời khóa biểu cá nhân dạng danh sách và dạng lưới 12 tiết theo từng học kỳ/tuần, *so that* tôi biết chính xác giờ học và phòng học mỗi ngày.
  - *Acceptance Criteria:* Hiển thị đúng các lớp đang `ENROLLED` theo Thứ 2 → Thứ 7 và Tiết 1 → Tiết 12.
- **`US-STU-03`:** *As a* **Sinh viên**, *I want to* xem điểm thành phần, điểm tổng kết từng môn, GPA học kỳ, CPA tích lũy và tiến độ hoàn thành Chương trình đào tạo, *so that* tôi nắm được kết quả học tập và biết mình cần học lại/cải thiện môn nào.
  - *Acceptance Criteria:* Chỉ hiển thị điểm các lớp đã `PUBLISHED`/`LOCKED`; CPA tích lũy tự động lấy điểm cao nhất đối với các môn học lại/cải thiện.
- **`US-STU-04`:** *As a* **Sinh viên**, *I want to* tự cập nhật số điện thoại, email cá nhân, địa chỉ và ảnh đại diện của mình, *so that* nhà trường có thông tin liên lạc chính xác của tôi.
  - *Acceptance Criteria:* Chỉ sửa được 4 trường liên lạc của chính mình; không sửa được thông tin học vụ hay xem dữ liệu của sinh viên khác.

---

## 4. PERMISSION MATRIX (MA TRẬN PHÂN QUYỀN CHI TIẾT — MỤC 5.4)

**Ký hiệu quyền thao tác:**
- `V` (**View**): Xem / Tra cứu
- `C` (**Create**): Tạo mới (hoặc Import)
- `U` (**Update**): Cập nhật
- `D` (**Delete / Deactivate**): Xóa / Ngưng hoạt động / Hủy
- `P` (**Publish**): Công bố dữ liệu
- `L` (**Lock / Unlock**): Khóa / Mở khóa dữ liệu
- `O` (**Override**): Thao tác đặc biệt có ghi `AuditLog` (đăng ký/hủy hộ, sửa điểm sau công bố)
- `—`: Không có quyền (`HTTP 403 FORBIDDEN`)

| Module / Chức năng | Mã Permission (Backend) | Admin (`ADMIN`) | Phòng đào tạo (`ACADEMIC_AFFAIRS`) | Giảng viên (`LECTURER`) | Sinh viên (`STUDENT`) | Phạm vi dữ liệu bắt buộc kiểm tra ở Backend (Data Scope) |
|---|---|---|---|---|---|---|
| **1. Tài khoản (`User`)** | `USER_READ`, `USER_WRITE`, `USER_LOCK` | `V, C, U, L` | `V` *(khi tạo SV/GV)* | `V` *(cá nhân)* | `V` *(cá nhân)* | GV & SV chỉ xem/đổi mật khẩu tài khoản của chính mình |
| **2. Vai trò & Quyền (`Role`, `Permission`)** | `RBAC_READ`, `RBAC_WRITE` | `V, C, U, D` | `—` | `—` | `—` | Chỉ Admin toàn quyền |
| **3. Khoa, Bộ môn, Ngành, Lớp HC** | `ORG_READ`, `ORG_WRITE` | `V` | `V, C, U, D` | `V` | `V` | Danh mục chung: xem công khai cho người đã đăng nhập; chỉ PĐT sửa |
| **4. Hồ sơ Giảng viên** | `LECTURER_READ`, `LECTURER_WRITE` | `V` | `V, C, U, D` | `V` *(hồ sơ mình)* | `—` | GV chỉ xem chi tiết hồ sơ của chính mình |
| **5. Hồ sơ Sinh viên & Trạng thái học vụ** | `STUDENT_READ`, `STUDENT_WRITE`, `STUDENT_SELF_UPDATE` | `V` | `V, C, U, D` *(+ Import)* | `V` *(SV thuộc lớp mình dạy)* | `V, U` *(chỉ 4 trường liên lạc của mình)* | SV chỉ đọc hồ sơ `student.user_id == currentUserId`; GV chỉ xem thông tin cơ bản của SV đang `ENROLLED` trong lớp mình dạy |
| **6. Học phần, Tiên quyết & Thành phần điểm** | `COURSE_READ`, `COURSE_WRITE` | `V` | `V, C, U, D` | `V` | `V` | Chỉ PĐT được cấu hình môn học, tiên quyết và trọng số điểm |
| **7. Chương trình đào tạo** | `CURRICULUM_READ`, `CURRICULUM_WRITE` | `V` | `V, C, U, D` | `V` | `V` *(ngành/khóa của mình)* | SV xem khung CTĐT và tiến độ của chính mình |
| **8. Năm học, Học kỳ & Đợt đăng ký** | `SEMESTER_READ`, `SEMESTER_WRITE` | `V` | `V, C, U, L` *(Đóng/Mở đợt)* | `V` | `V` | Chỉ PĐT được mở/đóng đợt đăng ký và chỉnh giới hạn tín chỉ |
| **9. Phòng học & Lớp học phần** | `SECTION_READ`, `SECTION_WRITE` | `V` | `V, C, U, D` | `V` *(lớp mở & lớp mình dạy)* | `V` *(danh sách lớp mở)* | Chỉ PĐT, Admin và GV phụ trách lớp (`section.lecturer_id == currentLecturerId`) mới xem được danh sách SV trong lớp |
| **10. Đăng ký học phần** | `ENROLLMENT_SELF`, `ENROLLMENT_OVERRIDE`, `ENROLLMENT_READ` | `V` | `V, O` *(Đăng ký/Hủy hộ có lý do)* | `V` *(danh sách SV lớp mình)* | `V, C, D` *(Tự ĐK / Tự hủy trong đợt)* | SV chỉ tự đăng ký/hủy cho chính mình khi `academic_status == ACTIVE` và đợt đăng ký đang mở |
| **11. Thời khóa biểu** | `SCHEDULE_READ` | `V` | `V` *(toàn trường)* | `V` *(TKB dạy của mình)* | `V` *(TKB học của mình)* | Lọc theo `currentStudentId` hoặc `currentLecturerId` |
| **12. Nhập điểm, Công bố & Khóa điểm** | `GRADE_WRITE`, `GRADE_PUBLISH`, `GRADE_LOCK`, `GRADE_OVERRIDE` | `—` | `V, P, L, O` *(Khóa sổ, sửa điểm có lý do)* | `V, U, P` *(Nhập & công bố lớp mình dạy khi `DRAFT`)* | `—` | GV chỉ nhập/công bố điểm khi `section.lecturer_id == currentLecturerId` **VÀ** `section.grade_status == DRAFT` |
| **13. Xem điểm & Kết quả học tập (GPA/CPA)** | `RESULT_READ_SELF`, `RESULT_READ_ALL` | `V` *(thống kê)* | `V` *(toàn bộ SV)* | `V` *(lớp mình dạy)* | `V` *(chỉ của mình, khi đã `PUBLISHED`/`LOCKED`)* | SV tuyệt đối không xem được điểm của SV khác hoặc điểm lớp đang `DRAFT` |
| **14. Báo cáo统计 & Cấu hình quy chế** | `REPORT_READ`, `CONFIG_WRITE` | `V, U` | `V, U` | `V` *(báo cáo lớp mình dạy)* | `—` | GV chỉ xem phổ điểm của lớp mình phụ trách |
| **15. Nhật ký kiểm toán (`AuditLog`)** | `AUDIT_READ` | `V` *(Chỉ đọc)* | `—` | `—` | `—` | Chỉ Admin xem; không vai trò nào được sửa/xóa |

---

## 5. CHECKLIST NGHIỆM THU GIAI ĐOẠN 2 (GATE G1-B)

- [x] **Functional Requirements (5.1):** Đã đặc tả đủ 30 FR (`FR-AUTH-01` → `FR-SYS-02`) với đầy đủ ID, Name, Actor, Priority, Description, Input, Processing, Output, Acceptance Criteria.
- [x] **Non-Functional Requirements (5.2):** Đã phân tích đủ 10 nhóm NFR kèm tiêu chí đo lường MVP và phương pháp kiểm chứng.
- [x] **User Stories (5.3):** Đã viết đầy đủ User Stories theo cấu trúc `As a / I want / So that` kèm Acceptance Criteria cho cả 4 vai trò.
- [x] **Permission Matrix (5.4):** Đã làm rõ cả quyền chức năng (`V/C/U/D/P/L/O`) và phạm vi dữ liệu (`Data Scope`) ở Backend.
