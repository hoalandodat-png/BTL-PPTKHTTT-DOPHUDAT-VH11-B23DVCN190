# GIAI ĐOẠN 1: DISCOVERY & REQUIREMENT ANALYSIS — HỆ THỐNG QUẢN LÝ ĐÀO TẠO SINH VIÊN PTIT

> [!NOTE]
> Tài liệu này tổng hợp đầy đủ **9 đầu ra của Giai đoạn 1** theo mục 4.5 của [Planning.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/Planning.md#L206-L217), dựa trên 24 quyết định đã xác nhận (`D-01` → `D-24`) trong [PROJECT_PLAN.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/docs/00-project-plan/PROJECT_PLAN.md).
> **Nguyên tắc minh bạch (Mục 3.1):** Dự án không sử dụng quy chế chính thức hay dữ liệu thật của PTIT. Tất cả tham số quy chế đào tạo và quy ước mã đều là **dữ liệu giả lập** (`⚠️ Assumption`) và được thiết kế theo hướng **cấu hình hóa trong Database**.

---

## 1. PROJECT OVERVIEW (TỔNG QUAN DỰ ÁN)

### 1.1. Bài toán quản lý đào tạo cần giải quyết
Trong đào tạo đại học theo **hệ thống tín chỉ**, hoạt động học vụ xoay quanh 4 luồng liên kết chặt chẽ:
1. **Thiết lập khung đào tạo:** Tổ chức Khoa – Bộ môn – Ngành – Lớp hành chính, danh mục học phần (kèm điều kiện tiên quyết/học trước) và khung chương trình đào tạo theo từng khóa.
2. **Tổ chức kế hoạch học kỳ:** Mở năm học/học kỳ, cấu hình đợt đăng ký tín chỉ, mở các lớp học phần, phân công giảng viên và xếp lịch học theo phòng học – tiết học – tuần học.
3. **Đăng ký học phần (nghiệp vụ phức tạp nhất):** Hàng trăm/nghìn sinh viên đăng ký vào các lớp học phần có giới hạn sĩ số trong một khoảng thời gian xác định. Hệ thống phải kiểm tra đồng thời nhiều ràng buộc (trạng thái học vụ, đợt đăng ký, tiên quyết/học trước, giới hạn tín chỉ tối đa, trùng lịch học, chống đăng ký trùng môn trong cùng kỳ, sĩ số lớp) và đảm bảo **không vượt sĩ số (overbooking)** khi có nhiều yêu cầu đồng thời.
4. **Đánh giá & công bố kết quả học tập:** Giảng viên nhập điểm theo thành phần điểm linh hoạt của từng môn, công bố điểm, Phòng đào tạo khóa sổ điểm; hệ thống tự động tính điểm tổng kết hệ 10, quy đổi điểm chữ, điểm hệ 4, xử lý môn học lại/cải thiện (lấy điểm cao nhất) và tính GPA học kỳ / CPA tích lũy.

### 1.2. Mục tiêu của Website
- **Mục tiêu nghiệp vụ:** Số hóa trọn vẹn chu trình đào tạo tín chỉ cốt lõi từ lúc tiếp nhận hồ sơ sinh viên → mở lớp học phần → đăng ký học phần → xếp thời khóa biểu → chấm điểm & tính GPA → báo cáo thống kê.
- **Mục tiêu kỹ thuật & Portfolio:** Thể hiện năng lực của một Software / Backend / Fullstack Developer qua:
  - Phân tích nghiệp vụ có chiều sâu, tách biệt quy tắc cố định và tham số cấu hình.
  - Thiết kế cơ sở dữ liệu chuẩn hóa (3NF), kiểm soát toàn vẹn dữ liệu và xử lý giao dịch đồng thời (ACID, Row-level Locking).
  - Bảo mật xác thực (JWT + Refresh Token trong `HttpOnly` Cookie) và phân quyền 2 lớp: **RBAC** kết hợp **Data-Scope Authorization** (kiểm tra quyền sở hữu/phụ trách dữ liệu).

### 1.3. Phạm vi dữ liệu chính cần quản lý
| Nhóm dữ liệu | Các thực thể cốt lõi |
|---|---|
| **Định danh & Phân quyền** | Tài khoản (`User`), Vai trò (`Role`), Quyền hạn (`Permission`), Phiên làm mới (`RefreshToken`), Nhật ký hệ thống (`AuditLog`) |
| **Cơ cấu tổ chức** | Khoa (`Faculty`), Bộ môn (`Department`), Ngành (`Major`), Lớp hành chính (`AcademicClass`), Hồ sơ giảng viên (`Lecturer`) |
| **Hồ sơ sinh viên** | Sinh viên (`Student`), Lịch sử biến động trạng thái học vụ (`StudentStatusHistory`) |
| **Chương trình & Học phần** | Học phần (`Course`), Điều kiện học phần (`CoursePrerequisite`), Chương trình đào tạo (`Curriculum`, `CurriculumCourse`) |
| **Kế hoạch học kỳ & Mở lớp** | Năm học (`AcademicYear`), Học kỳ (`Semester`), Đợt đăng ký (`RegistrationPeriod`), Phòng học (`Classroom`), Lớp học phần (`CourseSection`), Phân công giảng dạy (`TeachingAssignment`), Lịch học (`SectionSchedule`) |
| **Đăng ký học phần** | Kết quả đăng ký (`Enrollment`), Lịch sử thao tác đăng ký/hủy (`EnrollmentLog`) |
| **Điểm & Kết quả học tập** | Cấu hình thành phần điểm (`GradeComponent`), Thang điểm quy đổi (`GradingScale`), Điểm thành phần sinh viên (`StudentGrade`), Kết quả tổng kết học phần & GPA (`CourseResult`, `SemesterAcademicSummary`) |

---

## 2. STAKEHOLDER ANALYSIS (PHÂN TÍCH CÁC BÊN LIÊN QUAN)

| STT | Stakeholder | Vai trò trong dự án | Mối quan tâm chính | Mức ảnh hưởng | Cách hệ thống đáp ứng |
|---|---|---|---|---|---|
| 1 | **Chủ dự án / Developer (Bạn)** | BA, Architect, Fullstack Developer, QA | Phạm vi vừa sức 1 người nhưng đủ chiều sâu kỹ thuật (DB, Transaction, Security) để thuyết phục nhà tuyển dụng | Rất cao | Chia mốc M1 (Core) → M2 (Standard); kiến trúc Modular Monolith; tài liệu hóa rõ mọi quyết định |
| 2 | **Nhà tuyển dụng / Tech Interviewer** | Người đánh giá sản phẩm & source code | Tư duy thiết kế ERD, cách xử lý race condition khi đăng ký tín chỉ, phân quyền ở Backend, chất lượng code và test | Rất cao | Có tài liệu UML, ERD, Data Dictionary, ADR, kịch bản kiểm thử đồng thời (Testcontainers) và tài khoản demo sẵn |
| 3 | **Phòng Quản lý Đào tạo (Actor nghiệp vụ)** | Đơn vị vận hành cốt lõi | Quản lý danh mục, mở lớp không trùng phòng/giảng viên, kiểm soát đợt đăng ký, khóa điểm và tra cứu thống kê chính xác | Cao | Cung cấp bộ công cụ quản trị tập trung, kiểm tra trùng lịch 3 chiều, hỗ trợ đăng ký/hủy hộ và sửa điểm có ghi Audit Log |
| 4 | **Giảng viên (Actor nghiệp vụ)** | Người trực tiếp giảng dạy và đánh giá | Xem đúng lớp mình dạy, lịch dạy rõ ràng, nhập điểm nhanh (trên web hoặc import Excel), tránh sửa nhầm sau khi đã công bố | Cao | Phân quyền theo lớp được phân công; quy trình điểm 3 bước (`DRAFT` → `PUBLISHED` → `LOCKED`); hỗ trợ import điểm All-or-Nothing |
| 5 | **Sinh viên (Actor nghiệp vụ)** | Người học và sử dụng dịch vụ đào tạo | Đăng ký học phần minh bạch, biết ngay lý do nếu không đăng ký được, xem thời khóa biểu và bảng điểm/GPA chính xác trên cả máy tính và điện thoại | Cao | Thông báo lỗi nghiệp vụ rõ ràng (trùng lịch, thiếu môn tiên quyết, hết chỗ, quá tín chỉ); giao diện Sinh viên hỗ trợ Responsive |
| 6 | **Quản trị hệ thống – Admin (Actor nghiệp vụ)** | Người quản lý tài khoản, phân quyền và an toàn hệ thống | Cấp phát/khóa tài khoản tức thì, phân quyền RBAC chuẩn, tra cứu nhật ký hoạt động khi có tranh chấp dữ liệu | Trung bình | Cơ chế thu hồi Refresh Token ngay khi khóa tài khoản; quản lý RBAC và màn hình tra cứu Audit Log |

---

## 3. KHẢO SÁT & PHÂN TÍCH ACTOR (MỤC 4.2)

Theo mục 4.2 của `Planning.md`, dưới đây là phân tích đầy đủ 7 nhóm người dùng tiềm năng và quyết định đưa vào MVP (theo **D-07**):

### 3.1. Nhóm Actor đưa vào MVP (4 Actor — ✅ Confirmed D-07)

#### Actor 1: Admin (Quản trị hệ thống)
- **Vai trò:** Quản trị viên kỹ thuật và bảo mật của hệ thống.
- **Mục tiêu sử dụng:** Đảm bảo tài khoản người dùng, vai trò, quyền hạn, cấu hình quy chế hệ thống và nhật ký kiểm toán hoạt động ổn định, an toàn.
- **Trách nhiệm:** Quản lý vòng đời tài khoản (`User`), gán vai trò (`Role` – `Permission`), cấu hình tham số hệ thống (thang điểm, quy tắc tính điểm học lại), giám sát `AuditLog`.
- **Quyền hạn:** Toàn quyền trên phân hệ Quản trị tài khoản, Phân quyền RBAC, Cấu hình hệ thống và Nhật ký hoạt động. **Không** trực tiếp nhập điểm hay đăng ký học phần thay sinh viên (để đảm bảo tách biệt nhiệm vụ – Separation of Duties với Phòng đào tạo).
- **Dữ liệu được phép truy cập:** `users`, `roles`, `permissions`, `user_roles`, `role_permissions`, `audit_logs`, `grading_scale`, `system_configs`.
- **Chức năng được phép thực hiện:** Tạo/sửa/khóa/mở khóa tài khoản; đặt lại mật khẩu; quản lý Role & Permission; cấu hình thang điểm; tra cứu Audit Log.

#### Actor 2: Phòng Quản lý Đào tạo (`ACADEMIC_AFFAIRS`)
- **Vai trò:** Điều phối viên nghiệp vụ đào tạo toàn trường.
- **Mục tiêu sử dụng:** Vận hành toàn bộ chu trình học vụ từ danh mục khoa/ngành, hồ sơ sinh viên, chương trình đào tạo, mở lớp học phần, đợt đăng ký đến khóa sổ điểm và thống kê.
- **Trách nhiệm:** Thiết lập dữ liệu gốc học vụ; mở học kỳ & đợt đăng ký; mở lớp học phần & xếp lịch không xung đột; xử lý ngoại lệ (đăng ký/hủy hộ sinh viên, mở khóa hoặc điều chỉnh điểm sau công bố có lý do).
- **Quyền hạn:** CRUD trên các phân hệ Tổ chức, Sinh viên, Học phần, Chương trình đào tạo, Năm học/Học kỳ, Lớp học phần, Lịch học; quyền đặc biệt (Override có ghi Audit Log) trên Đăng ký học phần và Điểm.
- **Dữ liệu được phép truy cập:** Toàn bộ dữ liệu học vụ của tất cả các Khoa/Ngành/Sinh viên/Giảng viên/Lớp học phần.
- **Chức năng được phép thực hiện:** Quản lý Khoa/Bộ môn/Ngành/Lớp HC; quản lý & import hồ sơ sinh viên; quản lý học phần & điều kiện tiên quyết; quản lý chương trình đào tạo; mở học kỳ & đợt đăng ký; mở lớp học phần, gán giảng viên, xếp phòng & lịch học; đăng ký/hủy học phần hộ sinh viên; khóa bảng điểm, sửa điểm đặc biệt (kèm lý do); xem báo cáo thống kê.

#### Actor 3: Giảng viên (`LECTURER`)
- **Vai trò:** Cán bộ giảng dạy phụ trách các lớp học phần cụ thể trong từng học kỳ.
- **Mục tiêu sử dụng:** Nắm lịch giảng dạy, danh sách sinh viên của lớp mình phụ trách và thực hiện nhập/công bố điểm chính xác.
- **Trách nhiệm:** Giảng dạy theo thời khóa biểu; nhập điểm thành phần (trực tiếp hoặc import Excel) và công bố điểm đúng hạn cho các lớp học phần được phân công.
- **Quyền hạn:** Chỉ xem và thao tác trong **phạm vi các lớp học phần được phân công** (`TeachingAssignment`). Chỉ được sửa điểm khi bảng điểm của lớp ở trạng thái `DRAFT`.
- **Dữ liệu được phép truy cập:** Hồ sơ cá nhân của chính giảng viên; danh sách lớp học phần mình dạy; danh sách sinh viên (Mã SV, Họ tên, Lớp HC, Email) và bảng điểm của các lớp học phần đó. **Không** xem hay sửa được điểm của lớp do giảng viên khác phụ trách.
- **Chức năng được phép thực hiện:** Xem thời khóa biểu giảng dạy; xem danh sách lớp học phần được phân công & danh sách sinh viên trong lớp; nhập/sửa điểm thành phần (khi `DRAFT`); import bảng điểm từ Excel; công bố điểm (`DRAFT` → `PUBLISHED`); xem thống kê phân phổ điểm của lớp mình dạy.

#### Actor 4: Sinh viên (`STUDENT`)
- **Vai trò:** Người học tại trường.
- **Mục tiêu sử dụng:** Theo dõi chương trình đào tạo, đăng ký học phần mỗi kỳ, xem thời khóa biểu và tra cứu điểm / kết quả tích lũy.
- **Trách nhiệm:** Tự đăng ký/hủy học phần đúng quy chế và đúng thời hạn đợt đăng ký; giữ thông tin liên lạc cá nhân chính xác.
- **Quyền hạn:** Chỉ truy cập dữ liệu của **chính bản thân mình**; chỉ được đăng ký/hủy học phần khi có trạng thái học vụ `ACTIVE` và đang trong đợt đăng ký mở.
- **Dữ liệu được phép truy cập:** Hồ sơ cá nhân của chính mình; khung chương trình đào tạo của ngành/khóa mình thuộc về; danh sách lớp học phần mở trong kỳ; kết quả đăng ký, thời khóa biểu và bảng điểm (các lớp đã `PUBLISHED` hoặc `LOCKED`) của chính mình. **Tuyệt đối không** xem được hồ sơ hay điểm của sinh viên khác.
- **Chức năng được phép thực hiện:** Xem và cập nhật thông tin liên lạc cá nhân (SĐT, email cá nhân, địa chỉ, ảnh đại diện); xem chương trình đào tạo & môn tiên quyết; tra cứu lớp học phần mở trong kỳ; đăng ký & hủy đăng ký học phần; xem thời khóa biểu cá nhân; xem điểm thành phần, điểm tổng kết, GPA học kỳ và CPA tích lũy.

---

### 3.2. Nhóm Actor khảo sát nhưng KHÔNG đưa vào MVP v1.0 (Won't Have — ✅ Confirmed D-07)

| Actor | Vai trò trong thực tế | Lý do loại khỏi MVP v1.0 | Hướng mở rộng trong tương lai |
|---|---|---|---|
| **Cán bộ khoa/viện** | Quản lý bộ môn, đề xuất mở lớp và phân công giảng viên cấp Khoa | Trong MVP, Phòng đào tạo thực hiện tập trung để tránh quy trình phê duyệt nhiều cấp phức tạp cho 1 người code | Thêm Role `FACULTY_STAFF` giới hạn phạm vi dữ liệu theo `faculty_id` |
| **Cố vấn học tập** | Theo dõi tiến độ học tập của 1 lớp hành chính, tư vấn đăng ký môn | Bạn đã chốt bỏ ở **D-07** để tập trung vào 4 Actor cốt lõi | Thêm cột `advisor_lecturer_id` vào `academic_classes` và cấp quyền xem theo lớp HC |
| **Người quản lý xem báo cáo (Ban Giám hiệu)** | Xem dashboard tổng hợp toàn trường | Các báo cáo thống kê đã có trong màn hình của Phòng đào tạo và Admin | Tạo Role `REPORT_VIEWER` chỉ gồm các quyền `REPORT_READ` thông qua RBAC có sẵn |

---

## 4. KHẢO SÁT 13 PHÂN HỆ NGHIỆP VỤ & PHÂN LOẠI MoSCoW (MỤC 4.3)

Bảng tổng hợp mức độ ưu tiên MoSCoW và mốc phát hành của 13 phân hệ:

| Mã | Phân hệ | Ưu tiên MoSCoW | Mốc phát hành |
|---|---|---|---|
| **M01** | Quản lý tài khoản và phân quyền | **Must Have** | M1 (Core) + M2 (Quên mật khẩu, Audit Log) |
| **M02** | Quản lý khoa, bộ môn, ngành và lớp hành chính | **Must Have** | M1 (Core) |
| **M03** | Quản lý hồ sơ sinh viên | **Must Have** | M1 (Core) + M2 (Import Excel) |
| **M04** | Quản lý môn học / học phần (kèm tiên quyết) | **Must Have** | M1 (Core) |
| **M05** | Quản lý chương trình đào tạo | **Should Have** | M2 (Standard) |
| **M06** | Quản lý năm học, học kỳ và đợt đăng ký | **Must Have** | M1 (Core) |
| **M07** | Quản lý lớp học phần (kèm phân công GV & phòng học) | **Must Have** | M1 (Core) |
| **M08** | Quản lý đăng ký học phần | **Must Have** | M1 (Core) |
| **M09** | Quản lý thời khóa biểu | **Must Have** (xếp lịch & chống trùng) / **Should Have** (UI lưới TKB) | M1 (Core) / M2 (Standard) |
| **M10** | Quản lý điểm và kết quả học tập | **Must Have** | M1 (Nhập & xem điểm) + M2 (Công bố/Khóa, GPA, Import Excel) |
| **M11** | Quản lý học phí | **Could Have** | M3 (Extended – ngoài v1.0) |
| **M12** | Quản lý điểm danh | **Could Have** | M3 (Extended – ngoài v1.0) |
| **M13** | Báo cáo và thống kê | **Should Have** | M2 (Standard) |

---

### Chi tiết từng phân hệ (1 → 13)

#### Phân hệ 1: Quản lý tài khoản và phân quyền (`M01` — Must Have)
- **Mô tả nghiệp vụ:** Xác thực người dùng đăng nhập vào hệ thống, quản lý phiên làm việc bằng JWT + Refresh Token, đổi/đặt lại mật khẩu, quản lý trạng thái tài khoản (`ACTIVE`/`LOCKED`) và kiểm soát quyền truy cập theo mô hình RBAC kết hợp kiểm tra phạm vi dữ liệu.
- **Đối tượng tham gia:** Admin, Phòng đào tạo, Giảng viên, Sinh viên.
- **Dữ liệu đầu vào:** Tên đăng nhập/Email, mật khẩu, thông tin Role/Permission, trạng thái tài khoản.
- **Quy trình xử lý:**
  `User gửi thông tin đăng nhập → System kiểm tra tài khoản & trạng thái ACTIVE → Kiểm tra bcrypt password hash → Sinh Access Token (15p) + Refresh Token (7 ngày, lưu DB) gắn vào HttpOnly Cookie → Trả thông tin User & danh sách Role/Permission cho Frontend điều hướng.`
- **Kết quả:** Phiên đăng nhập hợp lệ hoặc thông báo từ chối truy cập (`401 Unauthorized` / `403 Forbidden`); nhật ký bảo mật được ghi nhận.
- **Trường hợp ngoại lệ:** Sai mật khẩu; tài khoản bị khóa (`LOCKED`) khi đang có phiên hoạt động (hệ thống thu hồi Refresh Token và chặn khi làm mới token); token hết hạn hoặc không hợp lệ; truy cập API vượt quyền.

#### Phân hệ 2: Quản lý khoa, bộ môn, ngành và lớp hành chính (`M02` — Must Have)
- **Mô tả nghiệp vụ:** Quản lý cấu trúc tổ chức học thuật của trường: Khoa (`Faculty`) → Bộ môn (`Department`) và Ngành đào tạo (`Major`) → Lớp hành chính (`AcademicClass`) theo từng khóa nhập học (`cohort_year`, ví dụ khóa 2022 – D22).
- **Đối tượng tham gia:** Phòng đào tạo (CRUD), Admin/Giảng viên/Sinh viên (Xem theo quyền).
- **Dữ liệu đầu vào:** Mã khoa, tên khoa; mã bộ môn, tên bộ môn, khoa trực thuộc; mã ngành, tên ngành, khoa trực thuộc; mã lớp hành chính (ví dụ `D22CQCN01-B`), ngành, khóa nhập học; hồ sơ giảng viên thuộc bộ môn.
- **Quy trình xử lý:**
  `Phòng đào tạo nhập thông tin Khoa/Bộ môn/Ngành/Lớp HC → System kiểm tra trùng mã và ràng buộc khóa ngoại → Lưu vào Database → Trả kết quả cập nhật danh mục.`
- **Kết quả:** Cây tổ chức Khoa – Bộ môn – Ngành – Lớp hành chính chuẩn xác để gắn Sinh viên, Giảng viên và Học phần.
- **Trường hợp ngoại lệ:** Trùng mã định danh; xóa Khoa/Bộ môn/Ngành/Lớp hành chính khi đã có Sinh viên hoặc Học phần trực thuộc (hệ thống chặn xóa vật lý, chỉ cho phép ngưng hoạt động `INACTIVE`).

#### Phân hệ 3: Quản lý hồ sơ sinh viên (`M03` — Must Have)
- **Mô tả nghiệp vụ:** Quản lý thông tin lý lịch, thông tin học vụ (mã SV, ngành, lớp hành chính, khóa học, trạng thái học vụ) và tài khoản đăng nhập tương ứng của sinh viên; hỗ trợ import danh sách sinh viên từ Excel/CSV ở M2.
- **Đối tượng tham gia:** Phòng đào tạo (Tạo mới, sửa toàn bộ, đổi trạng thái học vụ, import Excel), Sinh viên (Xem hồ sơ của mình, tự sửa thông tin liên lạc).
- **Dữ liệu đầu vào:** Mã SV (ví dụ `B22DCCN001`), họ tên, ngày sinh, giới tính, CCCD, email trường, email cá nhân, SĐT, địa chỉ, lớp hành chính, ngành, khóa, trạng thái học vụ (`ACTIVE`, `ON_LEAVE`, `SUSPENDED`, `GRADUATED`, `DROPPED_OUT`).
- **Quy trình xử lý:**
  `Phòng đào tạo tạo hồ sơ SV (hoặc import file) → System kiểm tra trùng Mã SV / Email / CCCD và sự tồn tại của Lớp HC/Ngành → Tự động tạo tài khoản User gắn Role STUDENT trong cùng 1 Transaction → Lưu hồ sơ SV & lịch sử trạng thái → Trả kết quả.`
- **Kết quả:** Hồ sơ sinh viên gắn kết 1–1 với tài khoản đăng nhập và thuộc một Lớp hành chính / Ngành cụ thể.
- **Trường hợp ngoại lệ:** Trùng Mã SV, CCCD hoặc Email; file import có dòng sai định dạng (rollback toàn bộ file và trả danh sách lỗi theo dòng); sinh viên cố tình gửi request sửa trường học vụ không được phép.

#### Phân hệ 4: Quản lý môn học / học phần (`M04` — Must Have)
- **Mô tả nghiệp vụ:** Quản lý danh mục học phần toàn trường, số tín chỉ, số giờ lý thuyết/thực hành, bộ môn phụ trách, cấu hình thành phần điểm mặc định của môn và các điều kiện ràng buộc giữa các môn (`PREREQUISITE` – Tiên quyết, `COREQUISITE` – Học trước).
- **Đối tượng tham gia:** Phòng đào tạo (CRUD), Giảng viên & Sinh viên (Tra cứu).
- **Dữ liệu đầu vào:** Mã học phần (ví dụ `INT1306`), tên học phần, số tín chỉ (`> 0`), bộ môn quản lý, danh sách môn điều kiện kèm loại điều kiện (`PREREQUISITE` / `COREQUISITE`), danh sách thành phần điểm và trọng số (tổng = 100%).
- **Quy trình xử lý:**
  `Phòng đào tạo tạo/cập nhật Học phần & điều kiện tiên quyết + thành phần điểm → System kiểm tra mã môn duy nhất, tổng trọng số = 100%, và kiểm tra KHÔNG tạo chu trình phụ thuộc vòng (A cần B và B cần A) → Lưu Database → Trả kết quả.`
- **Kết quả:** Học phần sẵn sàng để đưa vào Chương trình đào tạo và mở Lớp học phần.
- **Trường hợp ngoại lệ:** Thiết lập môn tiên quyết tự trỏ vào chính nó hoặc tạo vòng lặp `A → B → A`; tổng trọng số các thành phần điểm khác 100%; sửa/xóa học phần đã có lớp học phần mở trong các kỳ.

#### Phân hệ 5: Quản lý chương trình đào tạo (`M05` — Should Have, M2)
- **Mô tả nghiệp vụ:** Xác định khung chương trình đào tạo cho từng **Ngành + Khóa nhập học** (ví dụ: Chương trình ngành CNTT áp dụng cho khóa 2022), gồm danh sách các học phần bắt buộc/tự chọn và học kỳ khuyến nghị học (Học kỳ 1 → Học kỳ 8/9).
- **Đối tượng tham gia:** Phòng đào tạo (CRUD), Sinh viên & Giảng viên (Xem khung chương trình và tiến độ hoàn thành).
- **Dữ liệu đầu vào:** Mã chương trình, tên chương trình, ngành, khóa áp dụng, tổng số tín chỉ yêu cầu, danh sách học phần thuộc chương trình kèm học kỳ khuyến nghị và loại học phần (`REQUIRED` / `ELECTIVE`).
- **Quy trình xử lý:**
  `Phòng đào tạo khởi tạo Chương trình đào tạo cho Ngành + Khóa và gán danh sách Học phần → System kiểm tra không trùng lặp học phần trong cùng 1 chương trình → Lưu Database → Khi Sinh viên xem, System đối chiếu với kết quả học tập để hiển thị trạng thái từng môn (Đã qua / Đang học / Chưa học).`
- **Kết quả:** Khung chương trình đào tạo rõ ràng theo từng khóa và màn hình theo dõi tiến độ học tập cho sinh viên.
- **Trường hợp ngoại lệ:** Tạo 2 chương trình đào tạo chính thức trùng cùng 1 cặp `(Ngành, Khóa áp dụng)`; thêm 1 học phần 2 lần vào cùng 1 chương trình.

#### Phân hệ 6: Quản lý năm học, học kỳ và đợt đăng ký (`M06` — Must Have)
- **Mô tả nghiệp vụ:** Quản lý niên lịch học vụ gồm Năm học (ví dụ `2026-2027`), Học kỳ (Học kỳ 1, Học kỳ 2, Học kỳ Hè), cấu hình giới hạn tín chỉ tối thiểu/tối đa của học kỳ đó và thời gian mở/đóng **Đợt đăng ký học phần**.
- **Đối tượng tham gia:** Phòng đào tạo (CRUD & đóng/mở đợt đăng ký), Giảng viên & Sinh viên (Xem thông tin học kỳ hiện hành).
- **Dữ liệu đầu vào:** Mã học kỳ (ví dụ `20261`), tên học kỳ, năm học, loại học kỳ (`REGULAR` / `SUMMER`), ngày bắt đầu/kết thúc học kỳ, số tín chỉ tối thiểu – tối đa (`min_credits`, `max_credits`), thời gian bắt đầu – kết thúc đợt đăng ký (`registration_start_at`, `registration_end_at`), trạng thái học kỳ (`UPCOMING`, `ONGOING`, `CLOSED`).
- **Quy trình xử lý:**
  `Phòng đào tạo thiết lập Năm học, Học kỳ và cấu hình Đợt đăng ký → System kiểm tra tính hợp lệ của các mốc thời gian (bắt đầu < kết thúc, min_credits <= max_credits) → Lưu Database → Dùng làm căn cứ kiểm tra khi Sinh viên đăng ký/hủy học phần.`
- **Kết quả:** Học kỳ và đợt đăng ký có hiệu lực kiểm soát toàn bộ hoạt động mở lớp và đăng ký tín chỉ.
- **Trường hợp ngoại lệ:** Ngày kết thúc nhỏ hơn ngày bắt đầu; đóng học kỳ khi vẫn còn lớp học phần chưa khóa điểm.

#### Phân hệ 7: Quản lý lớp học phần (`M07` — Must Have)
- **Mô tả nghiệp vụ:** Trong mỗi học kỳ, Phòng đào tạo mở một hoặc nhiều **Lớp học phần** (`CourseSection`) cho từng môn học, thiết lập sĩ số tối đa, phân công Giảng viên giảng dạy và gán Phòng học + Lịch học theo tuần/thứ/tiết.
- **Đối tượng tham gia:** Phòng đào tạo (Tạo, sửa, phân công GV, xếp lịch, hủy lớp), Giảng viên & Sinh viên (Xem danh sách lớp mở).
- **Dữ liệu đầu vào:** Mã lớp học phần (ví dụ `INT1306-20261-01`), học phần, học kỳ, số nhóm, sĩ số tối đa (`max_capacity`), giảng viên phụ trách, danh sách buổi học trong tuần `(day_of_week, start_period, end_period, start_week, end_week, classroom_id)`, trạng thái lớp (`OPEN`, `CLOSED`, `CANCELLED`).
- **Quy trình xử lý:**
  `Phòng đào tạo tạo Lớp học phần, gán Giảng viên và thêm Lịch học/Phòng học → System kiểm tra sức chứa phòng học >= sĩ số tối đa của lớp, kiểm tra KHÔNG trùng lịch Phòng học và KHÔNG trùng lịch Giảng viên trong cùng học kỳ (xét giao đoạn tuần và giao đoạn tiết) → Lưu Database → Trả kết quả.`
- **Kết quả:** Lớp học phần hợp lệ, không xung đột phòng học hay giảng viên, sẵn sàng cho sinh viên đăng ký.
- **Trường hợp ngoại lệ:** Sĩ số tối đa vượt sức chứa phòng học; phòng học đã có lớp khác học trùng thứ/tiết/tuần; giảng viên bị phân công dạy 2 lớp trùng thứ/tiết/tuần; giảm sĩ số tối đa xuống thấp hơn số sinh viên đã đăng ký hiện tại.

#### Phân hệ 8: Quản lý đăng ký học phần (`M08` — Must Have, Trọng tâm kỹ thuật)
- **Mô tả nghiệp vụ:** Sinh viên tự đăng ký hoặc hủy đăng ký lớp học phần trong đợt đăng ký đang mở; Phòng đào tạo có thể đăng ký/hủy hộ (có ghi lý do). Hệ thống đảm bảo toàn vẹn dữ liệu và chống vượt sĩ số khi nhiều sinh viên cùng đăng ký suất cuối cùng.
- **Đối tượng tham gia:** Sinh viên (Tự đăng ký / tự hủy trong đợt), Phòng đào tạo (Đăng ký / hủy hộ).
- **Dữ liệu đầu vào:** `student_id`, `section_id`, người thực hiện (`actor_user_id`), lý do (nếu PĐT thao tác hộ).
- **Quy trình xử lý (trong 1 Database Transaction có khóa dòng `SELECT ... FOR UPDATE` trên bản ghi `CourseSection`):**
  1. Kiểm tra trạng thái học vụ của sinh viên phải là `ACTIVE`.
  2. Kiểm tra đợt đăng ký của học kỳ đang mở (trừ khi PĐT thao tác hộ theo quyền).
  3. Khóa bản ghi `CourseSection` (`FOR UPDATE`), kiểm tra trạng thái lớp là `OPEN` và `current_enrolled < max_capacity`.
  4. Kiểm tra sinh viên chưa đăng ký lớp học phần nào của **cùng môn học đó** trong học kỳ hiện tại (chống đăng ký trùng môn).
  5. Kiểm tra điều kiện **Tiên quyết (`PREREQUISITE`)** (đã có kết quả qua môn, điểm ≥ D) và **Học trước (`COREQUISITE`)** (đã từng học và có điểm tổng kết).
  6. Kiểm tra tổng số tín chỉ đã đăng ký trong kỳ + số tín chỉ môn mới `<= max_credits` của học kỳ.
  7. Kiểm tra **trùng lịch học** giữa các buổi học của lớp mới với tất cả các lớp mà sinh viên đã đăng ký trong kỳ (giao tuần + cùng thứ + giao tiết).
  8. Tạo/cập nhật bản ghi `Enrollment` (`STATUS = ENROLLED`), tăng `current_enrolled = current_enrolled + 1`, ghi `EnrollmentLog` → Commit Transaction.
- **Kết quả:** Sinh viên đăng ký/hủy thành công, sĩ số hiện tại của lớp cập nhật tức thì và chính xác tuyệt đối.
- **Trường hợp ngoại lệ:** Vi phạm bất kỳ điều kiện nào từ bước 1 → 7; 2 sinh viên cùng bấm đăng ký vào 1 suất trống cuối cùng (nhờ khóa dòng `FOR UPDATE`, giao dịch đến sau sẽ đợi giao dịch đầu commit xong, đọc thấy `current_enrolled == max_capacity` và nhận thông báo lỗi `"Lớp học phần đã đủ sĩ số"` kèm rollback an toàn); sinh viên bấm hủy sau khi đợt đăng ký đã đóng.

#### Phân hệ 9: Quản lý thời khóa biểu (`M09` — Must Have phần dữ liệu & kiểm tra trùng / Should Have phần UI lưới TKB)
- **Mô tả nghiệp vụ:** Quản lý các ca học theo khung 12 tiết/ngày (Thứ 2 → Thứ 7), kiểm tra xung đột lịch khi mở lớp và khi đăng ký, đồng thời hiển thị Thời khóa biểu trực quan theo học kỳ/tuần cho Sinh viên và Giảng viên.
- **Đối tượng tham gia:** Phòng đào tạo (Xếp lịch), Sinh viên (Xem TKB học tập), Giảng viên (Xem TKB giảng dạy).
- **Dữ liệu đầu vào:** Học kỳ, tuần học (tùy chọn), mã Sinh viên hoặc mã Giảng viên.
- **Quy trình xử lý:**
  `Sinh viên/Giảng viên chọn Học kỳ (và tuần) → System truy vấn danh sách Lớp học phần đã đăng ký (ENROLLED) hoặc được phân công dạy kèm lịch học & phòng học → Trả dữ liệu để Frontend hiển thị dạng bảng lưới (Thứ × Tiết) hoặc danh sách.`
- **Kết quả:** Thời khóa biểu cá nhân hóa theo từng học kỳ và từng tuần học, ghi rõ Mã môn, Tên môn, Nhóm lớp, Phòng học, Giảng viên, Tiết học, Tuần học.
- **Trường hợp ngoại lệ:** Sinh viên chưa đăng ký môn nào trong kỳ (hiển thị trạng thái trống rõ ràng).

#### Phân hệ 10: Quản lý điểm và kết quả học tập (`M10` — Must Have)
- **Mô tả nghiệp vụ:** Giảng viên nhập điểm các thành phần điểm cho sinh viên trong lớp học phần mình phụ trách (nhập trực tiếp trên bảng web hoặc import Excel ở M2), hệ thống tự động tính điểm tổng kết hệ 10, điểm chữ, điểm hệ 4 và kết quả qua/trượt; thực hiện quy trình `DRAFT` → `PUBLISHED` → `LOCKED`; tính GPA học kỳ và CPA tích lũy (có xử lý môn học lại lấy điểm cao nhất).
- **Đối tượng tham gia:** Giảng viên (Nhập điểm, import điểm, công bố điểm), Phòng đào tạo (Khóa sổ điểm, mở khóa hoặc sửa điểm đặc biệt có ghi lý do), Sinh viên (Xem điểm các lớp đã công bố/khóa và bảng điểm tích lũy).
- **Dữ liệu đầu vào:** Điểm từng thành phần (`0.0` → `10.0`, làm tròn 1 chữ số thập phân) của từng sinh viên trong lớp học phần; hành động chuyển trạng thái (`PUBLISH` / `LOCK` / `UNLOCK`); lý do chỉnh sửa (nếu sửa sau khi công bố/khóa).
- **Quy trình xử lý:**
  `Giảng viên nhập điểm thành phần (khi lớp ở trạng thái DRAFT) → System kiểm tra quyền phụ trách lớp và miền giá trị [0.0, 10.0] → Khi đủ các thành phần điểm, System tính điểm tổng kết hệ 10 theo trọng số, kiểm tra điều kiện điểm liệt cuối kỳ (< 4.0), đối chiếu bảng GradingScale ra điểm chữ & hệ 4 → Khi chuyển sang PUBLISHED/LOCKED, System cập nhật GPA học kỳ & CPA tích lũy (chỉ lấy lần học có điểm cao nhất đối với môn học lại) → Trả kết quả.`
- **Kết quả:** Bảng điểm lớp học phần, bảng điểm cá nhân theo học kỳ và bảng điểm tích lũy toàn khóa của sinh viên.
- **Trường hợp ngoại lệ:** Điểm nhập ngoài khoảng `[0, 10]`; giảng viên cố sửa điểm của lớp không được phân công hoặc lớp đã ở trạng thái `PUBLISHED`/`LOCKED`; sinh viên truy cập xem điểm khi lớp còn ở trạng thái `DRAFT` (hệ thống ẩn điểm chưa công bố).

#### Phân hệ 11: Quản lý học phí (`M11` — Could Have, M3 – Ngoài phạm vi v1.0)
- **Mô tả nghiệp vụ:** Tính học phí học kỳ dựa trên tổng số tín chỉ đã đăng ký nhân với đơn giá tín chỉ theo ngành/khóa, theo dõi trạng thái đóng học phí.
- **Lý do xếp vào Could Have (M3):** Phụ thuộc hoàn toàn vào kết quả đăng ký học phần; việc tạm hoãn sang M3 giúp tập trung làm thật sâu phân hệ Đăng ký học phần và Điểm ở M1/M2.

#### Phân hệ 12: Quản lý điểm danh (`M12` — Could Have, M3 – Ngoài phạm vi v1.0)
- **Mô tả nghiệp vụ:** Giảng viên điểm danh sinh viên theo từng buổi học của lớp học phần, tổng hợp tỷ lệ vắng để tính điểm chuyên cần hoặc cấm thi.
- **Lý do xếp vào Could Have (M3):** Nghiệp vụ tương đối độc lập, có thể bổ sung sau khi hoàn thành M2 mà không ảnh hưởng đến cấu trúc cốt lõi.

#### Phân hệ 13: Báo cáo và thống kê (`M13` — Should Have, M2)
- **Mô tả nghiệp vụ:** Cung cấp các báo cáo tổng hợp và thống kê trực quan phục vụ công tác quản lý đào tạo.
- **Đối tượng tham gia:** Phòng đào tạo, Admin, Giảng viên (chỉ xem thống kê lớp mình dạy).
- **Dữ liệu đầu vào:** Bộ lọc theo Học kỳ, Khoa, Ngành, Khóa học, Lớp học phần.
- **Quy trình xử lý:**
  `Người dùng chọn bộ lọc báo cáo → System thực thi các truy vấn tổng hợp (SQL Aggregation) trên PostgreSQL → Trả dữ liệu thống kê và biểu đồ.`
- **Kết quả:** 4 báo cáo cốt lõi ở M2:
  1. Thống kê số lượng sinh viên theo Khoa / Ngành / Khóa / Trạng thái học vụ.
  2. Thống kê tình hình đăng ký học phần theo học kỳ (sĩ số lấp đầy của các lớp học phần, danh sách lớp quá tải hoặc quá ít sinh viên).
  3. Phổ điểm và tỷ lệ qua môn / trượt môn theo Học phần hoặc Lớp học phần.
  4. Thống kê phân loại kết quả học tập (GPA Xuất sắc, Giỏi, Khá, Trung bình, Yếu) theo Học kỳ / Ngành.
- **Trường hợp ngoại lệ:** Truy vấn với bộ lọc không có dữ liệu (trả về tập rỗng và thông báo không có dữ liệu thống kê).

---

## 5. SCOPE DOCUMENT & BUSINESS REQUIREMENT DOCUMENT – BRD (MỤC 4.5.3 & 4.5.4)

### 5.1. Mục tiêu nghiệp vụ (Business Objectives)
- **BO-01:** Chuẩn hóa dữ liệu quản lý đào tạo (Tổ chức, Sinh viên, Giảng viên, Học phần, Chương trình đào tạo, Lớp học phần) trên một nguồn dữ liệu tập trung, loại bỏ tình trạng trùng lặp hoặc mâu thuẫn dữ liệu.
- **BO-02:** Tự động hóa kiểm tra toàn bộ điều kiện học vụ khi mở lớp (trùng phòng, trùng giảng viên) và khi sinh viên đăng ký học phần (tiên quyết, học trước, giới hạn tín chỉ, trùng lịch, sĩ số), đảm bảo **0% trường hợp vượt sĩ số tối đa** ngay cả khi có truy cập đồng thời.
- **BO-03:** Minh bạch hóa và bảo mật quy trình quản lý điểm với vòng đời 3 trạng thái (`DRAFT` → `PUBLISHED` → `LOCKED`), tự động hóa tính điểm tổng kết, quy đổi thang điểm và tính GPA/CPA chính xác (bao gồm trường hợp học lại/cải thiện).
- **BO-04:** Đảm bảo an toàn thông tin theo vai trò (RBAC) và phạm vi dữ liệu (Data-Scope), ghi vết đầy đủ (`AuditLog`) đối với mọi thao tác nhạy cảm của quản trị viên và phòng đào tạo.

### 5.2. Bảng phân định phạm vi sản phẩm (In-Scope vs Out-of-Scope)

| Hạng mục | Mốc M1 – Core (In-Scope) | Mốc M2 – Standard (In-Scope cho v1.0) | Mốc M3 / Future (Out-of-Scope v1.0) |
|---|---|---|---|
| **Xác thực & Phân quyền** | Đăng nhập, đăng xuất, đổi mật khẩu, JWT + Refresh Token (`HttpOnly`), RBAC + Data-Scope, khóa tài khoản | Quên mật khẩu qua email (Mailpit/SMTP), quản lý Role/Permission trên UI, tra cứu Audit Log | SSO (OAuth2 Google/Microsoft), 2FA/OTP |
| **Tổ chức & Hồ sơ** | CRUD Khoa, Bộ môn, Ngành, Lớp hành chính, Giảng viên, Sinh viên; vòng đời trạng thái học vụ SV | Import danh sách sinh viên từ CSV/Excel (Validate & Preview, All-or-Nothing) | Quản lý hồ sơ khen thưởng, kỷ luật, cố vấn học tập |
| **Học phần & CTĐT** | CRUD Học phần, điều kiện Tiên quyết (`PREREQUISITE`) & Học trước (`COREQUISITE`), cấu hình thành phần điểm | CRUD Chương trình đào tạo theo Ngành + Khóa; màn hình theo dõi tiến độ hoàn thành CTĐT của sinh viên | Quản lý đề cương chi tiết môn học (Syllabus PDF) |
| **Kế hoạch học kỳ & Lớp HP** | CRUD Năm học, Học kỳ, Đợt đăng ký, Phòng học, Lớp học phần, Phân công GV, Lịch học; kiểm tra trùng lịch Phòng & GV | Cấu hình linh hoạt giới hạn tín chỉ theo từng học kỳ trên UI | Tự động xếp thời khóa biểu bằng thuật toán tối ưu hóa |
| **Đăng ký học phần** | SV tự đăng ký/hủy trong đợt; kiểm tra đầy đủ điều kiện & xử lý đồng thời (`FOR UPDATE`); PĐT đăng ký/hủy hộ | Cảnh báo sinh viên đăng ký thiếu số tín chỉ tối thiểu khi kết thúc đợt đăng ký | Danh sách chờ (Waitlist) tự động đôn suất khi có người hủy |
| **Thời khóa biểu** | Dữ liệu lịch học theo thứ/tiết/tuần và API tra cứu TKB | Giao diện Thời khóa biểu dạng lưới trực quan (Thứ × 12 Tiết) cho SV và GV | Xuất lịch học ra file `.ics` (Google Calendar) |
| **Điểm & Kết quả học tập** | GV nhập điểm thành phần, tự động tính điểm tổng kết hệ 10 / điểm chữ / hệ 4; SV xem điểm | Quy trình `DRAFT` → `PUBLISHED` → `LOCKED`; PĐT sửa điểm sau khóa kèm lý do; tính GPA học kỳ & CPA tích lũy (học lại lấy điểm cao nhất); Import điểm từ Excel | Phúc khảo điểm trực tuyến, xét tốt nghiệp tự động |
| **Báo cáo & Mở rộng khác** | Số liệu tổng quan cơ bản | 4 báo cáo thống kê học vụ & phổ điểm | Học phí & cổng thanh toán, Điểm danh theo buổi, Xếp lịch thi |

---

## 6. DANH MỤC QUY TẮC NGHIỆP VỤ — BUSINESS RULES CATALOG (MỤC 4.5.7)

> [!IMPORTANT]
> Mỗi Business Rule dưới đây đều có mã định danh `BR-xx` để truy vết trực tiếp sang Functional Requirements (Giai đoạn 2), Use Case & Activity Diagram (Giai đoạn 3), Ràng buộc Database (Giai đoạn 4) và Test Cases (Giai đoạn 8).

### 6.1. Nhóm Xác thực, Tài khoản & Phân quyền (Auth & Security)
| Mã BR | Nội dung quy tắc nghiệp vụ | Phân loại | Nguồn |
|---|---|---|---|
| **BR-01** | Mọi mật khẩu người dùng phải được băm bằng thuật toán một chiều (`BCrypt`) trước khi lưu vào Database; tuyệt đối không lưu hoặc ghi log mật khẩu dạng văn bản thuần (plaintext). | ✅ Confirmed | `Planning.md` 9.3 |
| **BR-02** | Tài khoản có trạng thái `LOCKED` hoặc `INACTIVE` không được phép đăng nhập. Khi một tài khoản đang đăng nhập bị chuyển sang `LOCKED`, toàn bộ Refresh Token của tài khoản đó phải bị thu hồi ngay lập tức. | ✅ Confirmed | `Planning.md` 9.3, D-23 |
| **BR-03** | Mọi API riêng tư ở Backend đều phải kiểm tra cả 2 tầng: (1) Quyền chức năng theo RBAC (`Permission`), và (2) Quyền trên phạm vi dữ liệu (`Data-Scope`: Sinh viên chỉ đọc/sửa dữ liệu của chính mình; Giảng viên chỉ thao tác trên lớp học phần mình được phân công). | ✅ Confirmed | `Planning.md` 9.3 |
| **BR-04** | Mỗi tài khoản `STUDENT` gắn 1–1 với đúng một hồ sơ `Student`; mỗi tài khoản `LECTURER` gắn 1–1 với đúng một hồ sơ `Lecturer`. | ✅ Confirmed | D-07 |

### 6.2. Nhóm Cơ cấu tổ chức & Hồ sơ sinh viên (Organization & Student)
| Mã BR | Nội dung quy tắc nghiệp vụ | Phân loại | Nguồn |
|---|---|---|---|
| **BR-05** | Mã Khoa, Mã Bộ môn, Mã Ngành, Mã Lớp hành chính, Mã Giảng viên, Mã Sinh viên, Email và số CCCD phải là duy nhất trong toàn hệ thống. | ✅ Confirmed | D-20, D-21 |
| **BR-06** | Mỗi Bộ môn (`Department`) và mỗi Ngành (`Major`) thuộc về đúng một Khoa (`Faculty`). Mỗi Lớp hành chính (`AcademicClass`) thuộc về đúng một Ngành và một Khóa nhập học (`cohort_year`). | ✅ Confirmed | D-21 |
| **BR-07** | Không được xóa vật lý (`DELETE`) Khoa, Bộ môn, Ngành, Lớp hành chính hoặc Học phần nếu đã có bản ghi con (Sinh viên, Giảng viên, Lớp học phần) tham chiếu tới; chỉ được chuyển trạng thái sang ngưng hoạt động (`INACTIVE`). | 💡 Proposed → ✅ Confirmed | Toàn vẹn tham chiếu |
| **BR-08** | Hồ sơ sinh viên có 5 trạng thái học vụ: `ACTIVE` (Đang học), `ON_LEAVE` (Bảo lưu), `SUSPENDED` (Đình chỉ), `GRADUATED` (Đã tốt nghiệp), `DROPPED_OUT` (Thôi học). Mọi thay đổi trạng thái học vụ phải lưu lại lịch sử kèm thời điểm, người thực hiện và lý do. | ✅ Confirmed | D-17 |
| **BR-09** | Sinh viên chỉ được phép tự cập nhật 4 trường thông tin liên lạc: Số điện thoại, Email cá nhân, Địa chỉ liên hệ, Ảnh đại diện. Mọi trường pháp lý và học vụ khác chỉ Phòng đào tạo mới được quyền cập nhật. | ✅ Confirmed | D-18 |

### 6.3. Nhóm Học phần & Chương trình đào tạo (Course & Curriculum)
| Mã BR | Nội dung quy tắc nghiệp vụ | Phân loại | Nguồn |
|---|---|---|---|
| **BR-10** | Mỗi học phần có số tín chỉ (`credits`) là số nguyên dương (`>= 1`). | ✅ Confirmed | Quy tắc chung |
| **BR-11** | Quan hệ điều kiện giữa các học phần gồm 2 loại: `PREREQUISITE` (Học phần tiên quyết: sinh viên phải đã học và đạt điểm tổng kết từ `D / 1.0` trở lên) và `COREQUISITE` (Học phần học trước: sinh viên đã từng đăng ký học và đã có điểm tổng kết, không bắt buộc qua môn). | ✅ Confirmed (⚠️ Assumption) | D-11 |
| **BR-12** | Đồ thị điều kiện tiên quyết/học trước giữa các học phần không được phép có chu trình (không có môn tự làm điều kiện của chính nó hoặc vòng lặp `A → B → ... → A`). | ✅ Confirmed | Logic toán học |
| **BR-13** | Mỗi học phần trong một Chương trình đào tạo (`Curriculum`) chỉ xuất hiện tối đa 1 lần và được gắn với một học kỳ khuyến nghị (`recommended_semester` từ 1 đến 10) cùng phân loại `REQUIRED` (Bắt buộc) hoặc `ELECTIVE` (Tự chọn). | ✅ Confirmed | Mục 7.1, 7.3 |

### 6.4. Nhóm Học kỳ, Lớp học phần & Thời khóa biểu (Semester, Section & Schedule)
| Mã BR | Nội dung quy tắc nghiệp vụ | Phân loại | Nguồn |
|---|---|---|---|
| **BR-14** | Một ngày học chia thành **12 tiết** (Tiết 1–6: Sáng, Tiết 7–10: Chiều, Tiết 11–12: Tối), học từ Thứ 2 đến Thứ 7. Mỗi lịch học gồm `(day_of_week, start_period, end_period, start_week, end_week, classroom_id)` với `1 <= start_period <= end_period <= 12` và `1 <= start_week <= end_week`. | ✅ Confirmed (⚠️ Assumption) | D-19 |
| **BR-15** | Hai buổi học trong cùng một học kỳ bị coi là **trùng lịch** khi và chỉ khi: cùng `day_of_week` **VÀ** khoảng tuần giao nhau (`start_week_1 <= end_week_2 AND start_week_2 <= end_week_1`) **VÀ** khoảng tiết giao nhau (`start_period_1 <= end_period_2 AND start_period_2 <= end_period_1`). | ✅ Confirmed | D-19 |
| **BR-16** | Trong cùng một học kỳ, một **Phòng học (`Classroom`)** không được xếp cho 2 lớp học phần đang mở (`OPEN`/`CLOSED`) có lịch học trùng nhau theo quy tắc `BR-15`. Sức chứa của phòng học phải `>= max_capacity` của lớp học phần. | ✅ Confirmed | D-19 |
| **BR-17** | Trong cùng một học kỳ, một **Giảng viên (`Lecturer`)** không được phân công giảng dạy 2 lớp học phần đang mở có lịch học trùng nhau theo quy tắc `BR-15`. | ✅ Confirmed | D-19 |
| **BR-18** | Không được giảm `max_capacity` của một lớp học phần xuống nhỏ hơn số lượng sinh viên đang đăng ký hợp lệ (`current_enrolled`) của lớp đó. | ✅ Confirmed | Toàn vẹn dữ liệu |

### 6.5. Nhóm Đăng ký học phần & Xử lý đồng thời (Enrollment & Concurrency)
| Mã BR | Nội dung quy tắc nghiệp vụ | Phân loại | Nguồn |
|---|---|---|---|
| **BR-19** | Chỉ sinh viên có trạng thái học vụ `ACTIVE` mới được đăng ký học phần. | ✅ Confirmed | D-17 |
| **BR-20** | Sinh viên chỉ được tự đăng ký và tự hủy đăng ký khi thời điểm hiện tại nằm trong khoảng `[registration_start_at, registration_end_at]` của đợt đăng ký đang mở thuộc học kỳ đó. Sau thời điểm này, chỉ Phòng đào tạo mới có quyền đăng ký/hủy hộ (bắt buộc nhập lý do). | ✅ Confirmed (⚠️ Assumption) | D-09, D-13 |
| **BR-21** | Trong cùng một học kỳ, một sinh viên chỉ được có tối đa **01 bản ghi đăng ký đang hiệu lực (`ENROLLED`)** cho cùng một Học phần (`Course`), dù học phần đó mở nhiều lớp học phần (`CourseSection`) khác nhau. | ✅ Confirmed | `Planning.md` 7.9 |
| **BR-22** | Khi sinh viên đăng ký một lớp học phần, hệ thống phải kiểm tra sinh viên đã thỏa mãn **tất cả** các học phần điều kiện (`PREREQUISITE` và `COREQUISITE`) của môn học đó dựa trên kết quả các học kỳ trước. | ✅ Confirmed | D-11 |
| **BR-23** | Tổng số tín chỉ của các học phần đang `ENROLLED` trong học kỳ cộng với số tín chỉ của học phần mới đăng ký không được vượt quá `max_credits` của học kỳ đó (mặc định giả lập: kỳ chính `24` tín chỉ, kỳ hè `9` tín chỉ — lưu cấu hình theo từng `Semester`). Ngưỡng `min_credits` (mặc định `12` tín chỉ ở kỳ chính) chỉ dùng để cảnh báo/kiểm tra khi chốt đợt đăng ký, không chặn khi sinh viên đăng ký từng môn đầu tiên. | ✅ Confirmed (⚠️ Assumption) | D-12 |
| **BR-24** | Lịch học của lớp học phần mới đăng ký không được trùng (theo `BR-15`) với bất kỳ lớp học phần nào mà sinh viên đang `ENROLLED` trong cùng học kỳ. | ✅ Confirmed | D-19 |
| **BR-25** | Số lượng sinh viên `ENROLLED` trong một lớp học phần không bao giờ được vượt quá `max_capacity`. Thao tác kiểm tra sĩ số và ghi nhận đăng ký phải thực hiện trong một **Database Transaction có khóa bi quan (`SELECT ... FOR UPDATE`)** trên dòng `CourseSection` để chống Race Condition khi nhiều sinh viên cùng đăng ký suất cuối cùng. | ✅ Confirmed | `Planning.md` 7.9 |
| **BR-26** | Khi hủy đăng ký học phần, hệ thống không xóa vật lý bản ghi mà cập nhật trạng thái thành `CANCELLED` (kèm thời gian hủy, người hủy, lý do) và giảm `current_enrolled` đi 1 trong cùng một Transaction. Nếu sinh viên đăng ký lại chính lớp đó sau khi đã hủy, hệ thống kích hoạt lại hoặc tạo lượt đăng ký hợp lệ mà vẫn giữ lịch sử. | ✅ Confirmed | D-13 |
| **BR-27** | Sinh viên được phép đăng ký **học lại** (khi điểm tổng kết trước đó là `F`) hoặc **học cải thiện** (khi đã qua môn ở kỳ trước nhưng muốn nâng điểm) ở các học kỳ tiếp theo. Hệ thống lưu giữ toàn bộ kết quả của mọi lần học. | ✅ Confirmed (⚠️ Assumption) | D-10 |

### 6.6. Nhóm Quản lý điểm, Quy đổi & Tính GPA (Grade & Academic Result)
| Mã BR | Nội dung quy tắc nghiệp vụ | Phân loại | Nguồn |
|---|---|---|---|
| **BR-28** | Mỗi học phần có danh sách thành phần điểm (`GradeComponent`) với trọng số phần trăm sao cho tổng trọng số của học phần luôn bằng **100%** (mặc định giả lập: Chuyên cần 10%, Kiểm tra/Thực hành 20%, Giữa kỳ 20%, Cuối kỳ 50%). Điểm mỗi thành phần nằm trong đoạn `[0.0, 10.0]` và làm tròn đến **1 chữ số thập phân**. | ✅ Confirmed (⚠️ Assumption) | D-14, D-15 |
| **BR-29** | Điểm tổng kết hệ 10 được tính theo trung bình cộng có trọng số của các thành phần điểm (làm tròn 1 chữ số thập phân) và quy đổi sang điểm chữ / điểm hệ 4 theo bảng `GradingScale` trong DB (`8.5–10: A/4.0`, `8.0–8.4: B+/3.5`, `7.0–7.9: B/3.0`, `6.5–6.9: C+/2.5`, `5.5–6.4: C/2.0`, `5.0–5.4: D+/1.5`, `4.0–4.9: D/1.0`, `<4.0: F/0.0`). Tuy nhiên, nếu điểm thành phần **Cuối kỳ `< 4.0` (điểm liệt)** thì kết quả học phần bị xếp loại **F (0.0 – Không đạt)** bất kể điểm trung bình trọng số. | ✅ Confirmed (⚠️ Assumption) | D-14, D-15 |
| **BR-30** | Bảng điểm của mỗi lớp học phần có 3 trạng thái: `DRAFT` (Giảng viên phụ trách được nhập/sửa, Sinh viên chưa nhìn thấy điểm) → `PUBLISHED` (Đã công bố, Sinh viên xem được điểm, Giảng viên không tự sửa được nữa) → `LOCKED` (Phòng đào tạo khóa sổ điểm cuối kỳ). Mọi chỉnh sửa điểm sau khi `PUBLISHED` hoặc `LOCKED` chỉ do Phòng đào tạo thực hiện và bắt buộc ghi `AuditLog` (giá trị cũ, giá trị mới, người sửa, lý do). | ✅ Confirmed | D-16 |
| **BR-31** | **GPA học kỳ** (hệ 4, làm tròn 2 chữ số thập phân) tính trên tất cả học phần có kết quả đã công bố trong học kỳ đó. **CPA tích lũy** (hệ 4, làm tròn 2 chữ số thập phân) tính trên tất cả các học phần sinh viên đã học từ đầu khóa, trong đó nếu một học phần được học nhiều lần thì **chỉ lấy lần học có điểm hệ 4 cao nhất** (cấu hình được). Tổng số tín chỉ tích lũy chỉ tính các học phần đạt từ điểm `D (1.0)` trở lên và mỗi mã học phần chỉ tính tín chỉ tích lũy 1 lần. | ✅ Confirmed (⚠️ Assumption) | D-10, D-15 |

### 6.7. Nhóm Import dữ liệu & Kiểm toán hệ thống (Import & Audit)
| Mã BR | Nội dung quy tắc nghiệp vụ | Phân loại | Nguồn |
|---|---|---|---|
| **BR-32** | Chức năng Import CSV/Excel (hồ sơ sinh viên và bảng điểm lớp học phần) hoạt động theo nguyên tắc **Validate & Preview + All-or-Nothing**: kiểm tra toàn bộ các dòng trước; nếu có bất kỳ dòng nào vi phạm quy tắc nghiệp vụ thì trả về danh sách lỗi chi tiết theo số dòng và không ghi bất kỳ dòng nào vào Database. Mọi thao tác nhạy cảm (khóa tài khoản, đăng ký/hủy hộ, mở khóa/sửa điểm sau công bố) đều phải ghi vào bảng `audit_logs`. | ✅ Confirmed | D-09, D-16, D-22 |

---

## 7. DANH SÁCH FUNCTIONAL REQUIREMENTS SƠ BỘ (MỤC 4.5.9)

Danh sách này là cầu nối sang **Giai đoạn 2 (Requirement Specification)** — nơi mỗi mã `FR-xx` sẽ được đặc tả đầy đủ Input, Processing, Output và Acceptance Criteria:

| Nhóm phân hệ | Mã FR | Tên yêu cầu chức năng sơ bộ | Actor thực hiện | Mốc |Ưu tiên |
|---|---|---|---|---|---|
| **1. Auth & RBAC** | `FR-AUTH-01` | Đăng nhập và khởi tạo phiên JWT + Refresh Token (`HttpOnly` Cookie) | Tất cả Actor | M1 | Must |
| | `FR-AUTH-02` | Làm mới Access Token (Refresh Token Rotation) và Đăng xuất | Tất cả Actor | M1 | Must |
| | `FR-AUTH-03` | Đổi mật khẩu cá nhân (thu hồi các phiên cũ) | Tất cả Actor | M1 | Must |
| | `FR-AUTH-04` | Quên mật khẩu và đặt lại mật khẩu qua Email token | Tất cả Actor | M2 | Should |
| | `FR-AUTH-05` | Quản lý tài khoản người dùng (Tạo, xem, khóa/mở khóa, cấp lại mật khẩu) | Admin | M1 | Must |
| | `FR-AUTH-06` | Quản lý Vai trò (`Role`) và Quyền hạn (`Permission`) theo RBAC | Admin | M1/M2 | Must |
| **2. Organization** | `FR-ORG-01` | Quản lý danh mục Khoa (`Faculty`) và Bộ môn (`Department`) | Phòng đào tạo | M1 | Must |
| | `FR-ORG-02` | Quản lý danh mục Ngành đào tạo (`Major`) | Phòng đào tạo | M1 | Must |
| | `FR-ORG-03` | Quản lý Lớp hành chính (`AcademicClass`) theo Ngành & Khóa | Phòng đào tạo | M1 | Must |
| | `FR-ORG-04` | Quản lý hồ sơ Giảng viên (`Lecturer`) gắn với Bộ môn & Tài khoản | Phòng đào tạo | M1 | Must |
| **3. Student** | `FR-STU-01` | Quản lý hồ sơ Sinh viên (Tạo mới kèm tài khoản, cập nhật, tra cứu, lọc) | Phòng đào tạo | M1 | Must |
| | `FR-STU-02` | Quản lý chuyển đổi trạng thái học vụ của Sinh viên và lưu lịch sử | Phòng đào tạo | M1 | Must |
| | `FR-STU-03` | Sinh viên xem hồ sơ cá nhân và tự cập nhật thông tin liên lạc | Sinh viên | M1 | Must |
| | `FR-STU-04` | Import danh sách hồ sơ Sinh viên từ file CSV/Excel (Validate & All-or-Nothing) | Phòng đào tạo | M2 | Should |
| **4. Course** | `FR-CRS-01` | Quản lý danh mục Học phần (`Course`) và cấu hình thành phần điểm | Phòng đào tạo | M1 | Must |
| | `FR-CRS-02` | Thiết lập và kiểm tra điều kiện Tiên quyết / Học trước của Học phần | Phòng đào tạo | M1 | Must |
| **5. Curriculum** | `FR-CUR-01` | Quản lý Chương trình đào tạo theo Ngành & Khóa nhập học | Phòng đào tạo | M2 | Should |
| | `FR-CUR-02` | Xem khung Chương trình đào tạo và đối chiếu tiến độ hoàn thành của Sinh viên | Sinh viên, PĐT | M2 | Should |
| **6. Semester** | `FR-SEM-01` | Quản lý Năm học (`AcademicYear`) và Học kỳ (`Semester`) kèm giới hạn tín chỉ | Phòng đào tạo | M1 | Must |
| | `FR-SEM-02` | Cấu hình và đóng/mở Đợt đăng ký học phần (`RegistrationPeriod`) | Phòng đào tạo | M1 | Must |
| **7. Course Section** | `FR-SEC-01` | Quản lý danh mục Phòng học (`Classroom`) | Phòng đào tạo | M1 | Must |
| | `FR-SEC-02` | Mở Lớp học phần (`CourseSection`), phân công Giảng viên và xếp lịch học (kiểm tra trùng lịch Phòng & Giảng viên) | Phòng đào tạo | M1 | Must |
| | `FR-SEC-03` | Tra cứu danh sách Lớp học phần mở trong học kỳ và danh sách sinh viên của lớp | Tất cả Actor (theo quyền) | M1 | Must |
| **8. Enrollment** | `FR-ENR-01` | Sinh viên tự đăng ký Lớp học phần (kiểm tra 7 điều kiện nghiệp vụ & xử lý đồng thời) | Sinh viên | M1 | Must |
| | `FR-ENR-02` | Sinh viên tự hủy đăng ký Lớp học phần trong đợt đăng ký | Sinh viên | M1 | Must |
| | `FR-ENR-03` | Phòng đào tạo đăng ký / hủy đăng ký học phần hộ Sinh viên (kèm lý do & Audit Log) | Phòng đào tạo | M1 | Must |
| **9. Schedule** | `FR-SCH-01` | Xem Thời khóa biểu học tập cá nhân theo Học kỳ / Tuần (dạng danh sách & dạng lưới 12 tiết) | Sinh viên | M1/M2 | Must |
| | `FR-SCH-02` | Xem Thời khóa biểu giảng dạy của Giảng viên theo Học kỳ / Tuần | Giảng viên | M1/M2 | Must |
| **10. Grade** | `FR-GRD-01` | Giảng viên nhập và cập nhật điểm thành phần cho Lớp học phần phụ trách (khi `DRAFT`) | Giảng viên | M1 | Must |
| | `FR-GRD-02` | Import bảng điểm Lớp học phần từ file CSV/Excel (Validate & All-or-Nothing) | Giảng viên | M2 | Should |
| | `FR-GRD-03` | Tự động tính điểm tổng kết hệ 10, điểm chữ, điểm hệ 4 và xét điểm liệt cuối kỳ | Hệ thống | M1 | Must |
| | `FR-GRD-04` | Công bố điểm (`PUBLISHED`), Khóa sổ điểm (`LOCKED`) và chỉnh sửa điểm đặc biệt sau khóa (kèm Audit Log) | Giảng viên, PĐT | M1/M2 | Must |
| | `FR-GRD-05` | Sinh viên tra cứu điểm học kỳ, bảng điểm tích lũy, GPA học kỳ và CPA toàn khóa | Sinh viên, PĐT | M1/M2 | Must |
| **13. Reporting & System** | `FR-RPT-01` | Xem các báo cáo thống kê đào tạo (quân số SV, tỷ lệ lấp đầy lớp học phần, phổ điểm, phân loại GPA) | Phòng đào tạo, Admin, GV | M2 | Should |
| | `FR-SYS-01` | Cấu hình thang điểm quy đổi (`GradingScale`) và tham số quy chế đào tạo | Admin, PĐT | M2 | Should |
| | `FR-SYS-02` | Ghi nhận và tra cứu Nhật ký kiểm toán hệ thống (`AuditLog`) | Admin | M2 | Should |

---

## 8. DANH SÁCH OPEN QUESTIONS (MỤC 4.5.8)

### 8.1. Các câu hỏi của phạm vi v1.0 (M1 & M2) — ĐÃ GIẢI QUYẾT 100%
Toàn bộ 20 câu hỏi khảo sát của 3 vòng (`R1-1` → `R1-8`, `R2-1` → `R2-8`, `R3-1` → `R3-6`) đã được bạn xác nhận và chuyển thành quyết định `D-01` → `D-24`. **Không còn bất kỳ Open Question nào chặn việc chuyển sang Giai đoạn 2, 3, 4.**

### 8.2. Các câu hỏi để mở cho phạm vi tương lai (M3 – Extended, chưa cần trả lời hiện tại)
| Mã OQ | Câu hỏi mở (dành cho M3 sau khi xong v1.0) | Phân hệ liên quan |
|---|---|---|
| `OQ-M3-01` | Đơn giá học phí tính đồng nhất theo tín chỉ hay nhân hệ số theo từng loại học phần (lý thuyết / thực hành / học lại)? | M11 – Học phí |
| `OQ-M3-02` | Tỷ lệ vắng mặt tối đa khi điểm danh là bao nhiêu % thì sinh viên bị cấm thi cuối kỳ (ví dụ > 20%)? | M12 – Điểm danh |
| `OQ-M3-03` | Khi có danh sách chờ (Waitlist), suất trống được cấp tự động theo FIFO hay chỉ gửi thông báo cho sinh viên? | M08 – Mở rộng đăng ký |

---

## 9. TỔNG HỢP, KIỂM TRA TÍNH NHẤT QUÁN & CHECKLIST NGHIỆM THU GIAI ĐOẠN 1 (MỤC 3.3)

### 9.1. Kiểm tra tính nhất quán (Consistency Check)
1. **Giữa Actor và Phân hệ:** Cả 4 Actor trong MVP (`Admin`, `Phòng đào tạo`, `Giảng viên`, `Sinh viên`) đều có mục tiêu, quyền hạn, phạm vi dữ liệu và danh sách `FR` tương ứng; không có phân hệ `Must Have` hay `Should Have` nào thiếu Actor thực hiện.
2. **Giữa Quy tắc nghiệp vụ (`BR-01` → `BR-32`) và Yêu cầu chức năng (`FR`):**
   - Toàn bộ 8 điều kiện đăng ký học phần (`BR-19` → `BR-26`) đều được ánh xạ vào `FR-ENR-01`, `FR-ENR-02`, `FR-ENR-03`.
   - Quy tắc kiểm tra trùng lịch 3 chiều (`BR-14` → `BR-17`, `BR-24`) được ánh xạ nhất quán vào cả mở lớp (`FR-SEC-02`) và đăng ký học phần (`FR-ENR-01`).
   - Quy tắc thành phần điểm, điểm liệt, quy đổi thang điểm, vòng đời 3 trạng thái và GPA học lại (`BR-28` → `BR-31`) được ánh xạ đầy đủ vào `FR-GRD-01` → `FR-GRD-05`.
3. **Giữa nguyên tắc không có quy chế thật (`D-05`) và thiết kế nghiệp vụ:** Mọi hằng số quy chế (24 tín chỉ, 12 tiết, trọng số 10/20/20/50, điểm liệt 4.0, thang quy đổi A–F) đều được đánh dấu `⚠️ Assumption` và có yêu cầu cấu hình hóa (`FR-SEM-01`, `FR-CRS-01`, `FR-SYS-01`).

### 9.2. Checklist nghiệm thu Giai đoạn 1 (Gate G1-A)
- [x] **Project Overview:** Đã làm rõ bài toán đào tạo tín chỉ, mục tiêu sản phẩm và các nhóm dữ liệu chính.
- [x] **Stakeholder Analysis:** Đã phân tích đầy đủ 6 nhóm bên liên quan.
- [x] **Actor Analysis:** Đã phân tích 7 Actor và chốt 4 Actor đưa vào MVP v1.0.
- [x] **Business Modules & MoSCoW:** Đã phân tích chi tiết cả 13 phân hệ (Input, Quy trình, Output, Ngoại lệ, MoSCoW).
- [x] **Scope Document & BRD:** Đã phân định rõ M1 (Core), M2 (Standard) và M3 (Out-of-Scope).
- [x] **Business Rules Catalog:** Đã lập 32 quy tắc nghiệp vụ (`BR-01` → `BR-32`) kèm nhãn trạng thái rõ ràng.
- [x] **Preliminary Functional Requirements:** Đã lập danh sách 30 yêu cầu chức năng sơ bộ (`FR-AUTH` → `FR-SYS`).
- [x] **Open Questions:** Đã giải quyết 100% câu hỏi thuộc phạm vi v1.0.
