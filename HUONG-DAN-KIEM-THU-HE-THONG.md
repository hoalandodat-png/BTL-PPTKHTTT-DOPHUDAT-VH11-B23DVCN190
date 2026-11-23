# HƯỚNG DẪN KIỂM THỬ HỆ THỐNG QUẢN LÝ ĐÀO TẠO SINH VIÊN PTIT

Tài liệu này hướng dẫn đầy đủ cách kiểm thử **tự động (Automated Tests)** và **thủ công trên giao diện/API (End-to-End Manual Testing)** cho toàn bộ hệ thống tại `F:\QUẢN-LÝ-SINH-VIÊN-2`.

> **Tài liệu kỹ thuật tham khảo thêm:** [docs/06-testing/01-STAGE8-TEST-PLAN-AND-TEST-CASES.md](docs/06-testing/01-STAGE8-TEST-PLAN-AND-TEST-CASES.md)

---

## 1. Chuẩn bị trước khi kiểm thử

### 1.1. Kiểm tra PostgreSQL 18
- Đảm bảo service `postgresql-x64-18` đang chạy trên Windows (`localhost:5432`).
- Database chính thức: `ptit_training` (User ứng dụng: `ptit_user` / Password: `ptit_secret_password`, Admin: `postgres`).

### 1.2. Khởi chạy Backend (Terminal 1 — port `8080`)
Mở Terminal tại `F:\QUẢN-LÝ-SINH-VIÊN-2\backend` và chạy:
```powershell
java -jar target\training-management-1.0.0.jar
```
*(Lưu ý: Do đường dẫn dự án có dấu tiếng Việt `QUẢN-LÝ-SINH-VIÊN-2`, không chạy trực tiếp `mvn spring-boot:run`; nếu có sửa code Java, build lại rồi chạy `.jar`:)*
```powershell
& "C:\Users\Synful Orchild\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" -B -o package -DskipTests
java -jar target\training-management-1.0.0.jar
```
Kiểm tra Backend sẵn sàng: mở `http://localhost:8080/actuator/health` -> trả về `{"status":"UP"}` (Swagger UI: `http://localhost:8080/swagger-ui.html`).

### 1.3. Khởi chạy Frontend (Terminal 2 — port `3000`)
Mở tab Terminal thứ 2 tại `F:\QUẢN-LÝ-SINH-VIÊN-2\frontend` và chạy:
```powershell
npm.cmd run dev
```
Truy cập ứng dụng tại **`http://localhost:3000`**.

### 1.4. Tài khoản Demo phục vụ kiểm thử (Mật khẩu chung: `Ptit@12345`)

| Vai trò | Username | Ghi chú phục vụ test |
|---|---|---|
| `ADMIN` | `admin` | Quản lý user, khóa tài khoản, thêm/xóa quyền RBAC, Audit Log |
| `ACADEMIC_AFFAIRS` | `pdt01` | Khoa/Ngành/Lớp/GV, Học phần/Tiên quyết, Mở lớp, Xếp lịch thi, Khóa & Sửa điểm |
| `LECTURER` | `GV001`, `GV002` | Xem lớp kỳ `20261`, nhập/import/công bố điểm, TKB tuần, thêm tài khoản SV |
| `STUDENT` | `B22DCCN001` | Đã qua `INT1154` (điểm `A`), đủ điều kiện tiên quyết đăng ký `INT1306`, `INT1332` |
| `STUDENT` | `B22DCCN002` | Chưa qua `INT1154` (dùng để test chặn môn tiên quyết khi đăng ký `INT1306`/`INT1332`) |

---

## 2. Chạy Kiểm thử Tự động (Automated Tests)

### 2.1. Backend — Unit Test, Integration Test & Concurrency Test (H2 `MODE=PostgreSQL`)
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\latest\jdk-26"
cd F:\QUẢN-LÝ-SINH-VIÊN-2\backend
& "C:\Users\Synful Orchild\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd" -B -o test
```
**Phạm vi 12 test case tự động được thực thi:**
- `PrerequisiteCycleDetectorTest` (`1` test): Phát hiện và chặn chu trình đồ thị học phần tiên quyết (DAG).
- `GradeCalculatorDomainServiceTest` (`3` tests): Tính điểm tổng kết hệ 10, xét điểm liệt cuối kỳ `< 4.0`, tính GPA học kỳ và CPA tích lũy theo chính sách điểm cao nhất.
- `SecurityAndBusinessIntegrationTest` (`8` tests):
  - Xác thực Cookie `HttpOnly`, chặn `401` khi chưa đăng nhập, chặn `403` theo RBAC và Data-Scope (sinh viên xem hồ sơ SV khác, giảng viên xem/nhập điểm lớp không được phân công).
  - Khóa tài khoản thu hồi Refresh Token ngay lập tức.
  - Chặn `BR-09` khi sinh viên cố gửi trường đặc quyền (`fullName`, `academicStatus`).
  - Chặn trùng lịch phòng học khi mở lớp (`SCHEDULE_CLASSROOM_CONFLICT`).
  - Chặn đăng ký học phần thiếu tiên quyết (`ENROLL_PREREQUISITE_NOT_MET`), test đăng ký – hủy – đăng ký lại.
  - **Concurrency Test (10 luồng đồng thời tranh 1 suất cuối):** Chỉ đúng `1` sinh viên đăng ký thành công, `9` luồng còn lại bị từ chối sạch, `current_enrolled = 1`.
  - Import CSV sinh viên **All-or-Nothing** (1 dòng lỗi -> rollback toàn bộ).
  - Luồng điểm `DRAFT` (SV chưa thấy) -> `PUBLISHED` (GV bị chặn sửa) -> `LOCKED` -> PĐT điều chỉnh sau khóa có `AuditLog`.

**Kết quả đạt yêu cầu:** `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0` (`BUILD SUCCESS`).

### 2.2. Frontend — TypeScript Check & Production Build
```powershell
cd F:\QUẢN-LÝ-SINH-VIÊN-2\frontend
npm.cmd run typecheck
```
> **Lưu ý khi chạy `npm.cmd run build`:** Hãy dừng `npm.cmd run dev` (`Ctrl + C`) trước khi chạy `npm.cmd run build`, sau đó chạy lại `npm.cmd run dev` (vì `next build` ghi đè thư mục `.next`).

---

## 3. Kịch bản Kiểm thử Thủ công trên Trình duyệt (E2E Checklist)

### Nhóm A — Xác thực & Phân quyền (`/login`, `/forgot-password`)
- [ ] **TC-01 — Đăng nhập sai mật khẩu:** Nhập `admin` + mật khẩu sai -> Hiển thị thông báo lỗi rõ ràng, không chuyển trang.
- [ ] **TC-02 — Đăng nhập đúng vai trò:** Đăng nhập `admin` -> vào `/admin`; `pdt01` -> `/academic`; `GV001` -> `/lecturer`; `B22DCCN001` -> `/student`.
- [ ] **TC-03 — Chặn truy cập chéo cổng:** Đang đăng nhập tài khoản `STUDENT`, thử gõ trực tiếp URL `http://localhost:3000/admin` -> Không xem được dữ liệu quản trị.
- [ ] **TC-04 — Đổi mật khẩu:** Bấm **Đổi mật khẩu** ở góc dưới sidebar, nhập mật khẩu hiện tại sai -> báo lỗi; nhập đúng -> đổi thành công.

---

### Nhóm B — Cổng Sinh viên (`/student`)
- [ ] **TC-05 — Đăng ký học phần & Chặn tiên quyết:**
  1. Đăng nhập `B22DCCN002` (chưa học `INT1154`) -> vào **Đăng ký học phần** kỳ `20261`.
  2. Thử bấm **Đăng ký** môn `INT1306` hoặc `INT1332` -> Hệ thống chặn và báo thiếu học phần tiên quyết `INT1154`.
  3. Bấm **Đăng ký** môn `INT1154-20261-01` hoặc `INT1313-20261-01` -> Đăng ký thành công, sĩ số lớp tăng thêm `1`.
- [ ] **TC-06 — Kiểm tra giới hạn tín chỉ theo loại kỳ & hệ đào tạo:**
  - Học kỳ chính (`20261`) – Hệ cử nhân (`DCCN`): hiển thị `Tối thiểu 15 · Tối đa 25 TC`.
  - Học kỳ chính – Hệ kỹ sư (`DCAT`): định mức `Tối thiểu 16 · Tối đa 25 TC`.
  - Học kỳ phụ Hè (`20263`, khi PĐT mở ĐK): hiển thị `Không quy định tối thiểu · Tối đa 12 TC`, không cảnh báo thiếu tín chỉ tối thiểu.
  - Khi tổng tín chỉ + môn sắp đăng ký vượt mức tối đa -> nút chuyển sang khóa `Vượt 25 TC` / `Vượt 12 TC`.
- [ ] **TC-07 — Thời khóa biểu dạng tuần:**
  - Vào tab **Thời khóa biểu tuần**, bấm `< Tuần trước` / `Tuần sau >`, chọn từng tuần từ dropdown và chuyển sang `Cả học kỳ`.
  - Xác nhận cột `Thứ 2`–`Thứ 7` hiển thị đúng ngày thực tế của tuần đó và chỉ hiện các môn có hiệu lực trong tuần.
- [ ] **TC-08 — Lịch thi:**
  - Vào tab **Lịch thi** -> chỉ hiển thị lịch thi của các lớp sinh viên đã `ENROLLED` trong học kỳ đang chọn (ngày thi, giờ thi, phòng thi, hình thức).
- [ ] **TC-09 — Kết quả học tập & Chương trình đào tạo:**
  - Vào **Kết quả học tập** -> kiểm tra `B22DCCN001` thấy điểm `A (8.5 / 4.00)` kỳ `20252`; các lớp kỳ `20261` chưa công bố sẽ hiện `Chưa công bố` (không lộ điểm nháp `DRAFT`).
  - Vào **Chương trình đào tạo** -> kiểm tra trạng thái `Đã qua` / `Đang học` / `Chưa học`.
- [ ] **TC-10 — Tự cập nhật hồ sơ (`BR-09`):**
  - Vào **Hồ sơ cá nhân** -> chỉ sửa được SĐT, Email cá nhân, Địa chỉ, Ảnh đại diện; không sửa được Họ tên, Lớp HC, Trạng thái học vụ.

---

### Nhóm C — Cổng Giảng viên (`/lecturer`)
- [ ] **TC-11 — Chọn học kỳ & Nhập điểm từng sinh viên:**
  1. Đăng nhập `GV001`, xác nhận mặc định mở kỳ `20261`.
  2. Chọn lớp `INT1306-20261-01`, nhập 4 đầu điểm (`Chuyên cần 10%`, `Thực hành 20%`, `Giữa kỳ 20%`, `Cuối kỳ 50%`) cho sinh viên -> bấm **Lưu bảng điểm nháp**.
  3. Kiểm tra tự tính đúng `Tổng kết`, `Chữ`, `Đạt/Trượt`. Thử nhập điểm **Cuối kỳ `< 4.0`** (dù các điểm khác `10`) -> phải ra điểm chữ `F` và `Trượt` (điểm liệt).
- [ ] **TC-12 — Công bố điểm & Khóa chỉnh sửa GV:**
  - Bấm **Công bố điểm** -> trạng thái đổi sang `PUBLISHED`, các ô nhập điểm bị khóa không cho giảng viên sửa tiếp; kiểm tra bên tài khoản Sinh viên thấy ngay điểm vừa công bố.
- [ ] **TC-13 — Thời khóa biểu tuần Giảng viên:**
  - Vào tab **Thời khóa biểu tuần** -> hiển thị đúng các lớp giảng viên phụ trách theo tuần và ngày thực tế.
- [ ] **TC-14 — Giảng viên thêm tài khoản Sinh viên trắng:**
  1. Vào tab **Thêm tài khoản Sinh viên**, tạo SV mới (VD: `B23DCCN099`, để trống mật khẩu để dùng `Ptit@12345`).
  2. Đăng xuất và đăng nhập bằng `B23DCCN099` -> Xác nhận tài khoản hoàn toàn trắng (`0` TC, chưa có TKB/lịch thi/kết quả) và đăng ký được lớp mở kỳ `20261`.

---

### Nhóm D — Cổng Phòng Quản lý Đào tạo (`/academic`)
- [ ] **TC-15 — Thêm Khoa, Bộ môn, Ngành (Cử nhân/Kỹ sư), Lớp HC, Giảng viên:**
  - Vào tab **Khoa, Ngành, Lớp & GV**, tạo lần lượt 1 Khoa -> 1 Bộ môn -> 1 Ngành (thử chọn `Hệ kỹ sư`) -> 1 Lớp HC -> 1 Giảng viên mới. Thử đăng nhập bằng mã Giảng viên vừa tạo.
- [ ] **TC-16 — Tạo Học phần & Cấu hình Tiên quyết (DAG):**
  - Vào tab **Học phần & Tiên quyết**, tạo học phần mới (thành phần điểm chuẩn 10-20-20-50).
  - Cấu hình `Môn B` có tiên quyết là `Môn A`; sau đó thử cấu hình ngược lại `Môn A` có tiên quyết là `Môn B` -> Hệ thống phải **chặn vì tạo chu trình (DAG)**.
- [ ] **TC-17 — Mở lớp học phần & Kiểm tra trùng lịch 3 chiều:**
  - Vào tab **Lớp học phần & Khóa điểm**, thử mở lớp mới trùng **Phòng** hoặc trùng **Giảng viên** với lớp đã có cùng Thứ / Khoảng tuần / Khoảng tiết -> Phải báo lỗi xung đột (`409 Conflict`).
- [ ] **TC-18 — Đóng/Mở đợt đăng ký & Xếp lịch thi:**
  - Vào tab **Học kỳ & Đợt đăng ký**, thử bật/tắt đợt đăng ký kỳ `20263` (Học kỳ phụ Hè) và thử thêm lịch thi trùng phòng/ngày/giờ -> Phải chặn trùng phòng thi.
- [ ] **TC-19 — Khóa sổ điểm & Sửa điểm sau khóa:**
  - Với lớp đã `PUBLISHED`, PĐT bấm **Khóa sổ điểm** -> chuyển sang `LOCKED`, sau đó dùng chức năng điều chỉnh điểm (qua API/Swagger hoặc trang chi tiết) và kiểm tra ghi nhận trong Audit Log.
- [ ] **TC-20 — Import CSV Sinh viên (All-or-Nothing):**
  - Vào tab **Hồ sơ Sinh viên**, thử upload 1 file `.csv` có 2 dòng trong đó 1 dòng bị trùng CCCD/email -> Hệ thống báo lỗi và **không thêm bất kỳ dòng nào**.

---

### Nhóm E — Cổng Quản trị Hệ thống (`/admin`)
- [ ] **TC-21 — Khóa tài khoản tức thì:**
  - Vào **Quản lý Tài khoản**, bấm **Khóa tài khoản** với `B22DCCN002` (nhập lý do) -> Nếu `B22DCCN002` thử đăng nhập lại hoặc gọi `/api/v1/auth/refresh` phải bị chặn `403`. *(Nhớ bấm **Mở khóa** lại sau khi test xong).*
- [ ] **TC-22 — Thêm & Xóa quyền cho 4 vai trò (RBAC):**
  1. Vào tab **Vai trò & Quyền hạn (RBAC)**.
  2. Chọn 1 vai trò (VD: `LECTURER` hoặc `STUDENT`), dùng dropdown **Chọn quyền cần thêm** -> bấm **`+ Thêm quyền`**.
  3. Bấm dấu **`×`** trên quyền vừa thêm để xóa lại.
  4. Thử xóa `RBAC_MANAGE` của `ADMIN` -> Hệ thống chặn để tránh tự mất quyền quản trị.
- [ ] **TC-23 — Kiểm tra Nhật ký Kiểm toán (Audit Logs):**
  - Vào tab **Nhật ký Kiểm toán** -> Xác nhận ghi nhận đầy đủ các thao tác khóa user, thêm/xóa quyền RBAC, mở/đóng đợt đăng ký, điều chỉnh điểm kèm người thực hiện và giá trị trước/sau.

---

## 4. Kiểm thử API trực tiếp qua Swagger UI
1. Đăng nhập trên trình duyệt tại `http://localhost:3000/login` trước (để trình duyệt lưu cookie `access_token`), hoặc gọi `POST /api/v1/auth/login` trong Swagger UI tại `http://localhost:8080/swagger-ui.html`.
2. Nhấn **Try it out** -> **Execute** trên bất kỳ endpoint nào để kiểm tra HTTP status, JSON schema `ApiResponse` (`success`, `data`, `error`, `timestamp`) và mã lỗi nghiệp vụ.

---

## 5. Bảng ghi nhận kết quả kiểm thử (Mẫu nghiệm thu)

| Nhóm Test | Tổng số TC | Số TC Đạt (Pass) | Số TC Lỗi (Fail) | Người thực hiện | Ngày kiểm thử | Ghi chú |
|---|---:|---:|---:|---|---|---|
| Automated Backend Tests (`mvn test`) | 12 | | | | | |
| Automated Frontend (`typecheck` / `build`) | 2 | | | | | |
| Nhóm A — Auth & Phân quyền | 4 | | | | | |
| Nhóm B — Cổng Sinh viên | 6 | | | | | |
| Nhóm C — Cổng Giảng viên | 4 | | | | | |
| Nhóm D — Phòng Đào tạo | 6 | | | | | |
| Nhóm E — Quản trị Admin | 3 | | | | | |
