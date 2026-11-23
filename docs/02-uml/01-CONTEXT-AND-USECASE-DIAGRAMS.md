# GIAI ĐOẠN 3 (PHẦN 1): SYSTEM CONTEXT, USE CASE DIAGRAMS & USE CASE SPECIFICATIONS

> [!NOTE]
> Tài liệu này thực hiện mục **6.1 (System Context Diagram)**, **6.2 (Use Case Diagram)** và **6.3 (Use Case Specification)** của [Planning.md](file:///f:/QUẢN-LÝ-SINH-VIÊN-2/Planning.md#L286-L344), đồng thời đáp ứng yêu cầu bắt buộc về **Authentication & Authorization Use Case Diagram** tại mục 9.3.6.
> Mỗi sơ đồ đều có: **Mục đích, Phạm vi, Sơ đồ trực quan (Mermaid), Mã nguồn PlantUML chuẩn, Giải thích ký hiệu & quan hệ, và Kiểm tra tính nhất quán**.

---

## 1. SYSTEM CONTEXT DIAGRAM (MỤC 6.1)

### 1.1. Mục đích & Phạm vi
- **Mục đích:** Xác định ranh giới (System Boundary) của **Hệ thống Quản lý Đào tạo Sinh viên PTIT**, các nhóm người dùng tương tác trực tiếp (4 Actor) và các hệ thống bên ngoài có trao đổi dữ liệu.
- **Phạm vi:** Toàn bộ hệ thống ở mức hộp đen (Black-box), làm rõ luồng dữ liệu đầu vào / đầu ra giữa Hệ thống với Con người và Dịch vụ ngoài.

### 1.2. Sơ đồ trực quan (Mermaid)
```mermaid
flowchart LR
    ADMIN["👤 Admin<br/>(Quản trị hệ thống)"]
    AA["👤 Phòng Đào tạo<br/>(Academic Affairs)"]
    LEC["👤 Giảng viên<br/>(Lecturer)"]
    STU["👤 Sinh viên<br/>(Student)"]

    SYS(("🏛️ HỆ THỐNG QUẢN LÝ<br/>ĐÀO TẠO SINH VIÊN PTIT"))

    SMTP["📧 Hệ thống Email<br/>(SMTP / Mailpit)"]
    EXCEL["📄 Công cụ Bảng tính<br/>(File CSV / Excel .xlsx)"]

    ADMIN -- "Quản lý User, Role, Permission,<br/>Cấu hình thang điểm, Xem Audit Log" --> SYS
    SYS -- "Trạng thái TK, Nhật ký kiểm toán,<br/>Báo cáo hệ thống" --> ADMIN

    AA -- "Quản lý Khoa/Ngành/SV/GV/Môn/CTĐT,<br/>Mở học kỳ & lớp HP, ĐK hộ, Khóa điểm" --> SYS
    SYS -- "Danh sách học vụ, Cảnh báo trùng lịch,<br/>Báo cáo thống kê đào tạo" --> AA

    LEC -- "Xem lớp phân công & TKB dạy,<br/>Nhập điểm, Import điểm, Công bố điểm" --> SYS
    SYS -- "TKB giảng dạy, Danh sách SV lớp,<br/>Bảng điểm tổng kết tự động" --> LEC

    STU -- "Cập nhật liên lạc, Đăng ký/Hủy học phần,<br/>Tra cứu CTĐT, TKB, Điểm & GPA" --> SYS
    SYS -- "Kết quả đăng ký, TKB cá nhân,<br/>Bảng điểm học kỳ & CPA tích lũy" --> STU

    SYS -- "Gửi email đặt lại mật khẩu<br/>(Reset Password Token)" --> SMTP
    AA -- "Tải lên file danh sách SV" --> EXCEL
    LEC -- "Tải lên file điểm lớp học phần" --> EXCEL
    EXCEL -- "Dữ liệu CSV/XLSX đầu vào" --> SYS
```

### 1.3. Mã nguồn PlantUML
```plantuml
@startuml System_Context_Diagram
!theme plain
left to right direction
skinparam packageStyle rectangle

actor "Admin\n(Quản trị hệ thống)" as Admin
actor "Phòng Quản lý Đào tạo\n(Academic Affairs)" as AA
actor "Giảng viên\n(Lecturer)" as Lecturer
actor "Sinh viên\n(Student)" as Student

rectangle "HỆ THỐNG QUẢN LÝ ĐÀO TẠO SINH VIÊN PTIT" as PTIT_System #EEF4FF

rectangle "SMTP Server / Mailpit\n(External Email Service)" as SMTP #F9F9F9
rectangle "Microsoft Excel / CSV\n(File Import/Export)" as Excel #F9F9F9

Admin --> PTIT_System : Quản lý User, RBAC, Cấu hình, Xem Audit Log
PTIT_System --> Admin : Nhật ký kiểm toán, Trạng thái hệ thống

AA --> PTIT_System : Thiết lập Tổ chức, SV, Học phần, CTĐT,\nHọc kỳ, Lớp HP, ĐK hộ, Khóa điểm
PTIT_System --> AA : Kết quả kiểm tra trùng lịch, Báo cáo thống kê

Lecturer --> PTIT_System : Xem TKB dạy, Nhập/Import điểm, Công bố điểm
PTIT_System --> Lecturer : Danh sách SV lớp dạy, Phổ điểm lớp

Student --> PTIT_System : Sửa liên lạc, Đăng ký/Hủy học phần
PTIT_System --> Student : TKB cá nhân, Điểm, GPA/CPA, Tiến độ CTĐT

PTIT_System --> SMTP : Gửi link đặt lại mật khẩu (SMTP)
Excel <..> PTIT_System : Import danh sách SV & Bảng điểm (.xlsx/.csv)
@enduml
```

### 1.4. Giải thích ký hiệu & Kiểm tra tính nhất quán
- **Ký hiệu:** Nút trung tâm là hệ thống mục tiêu; 4 Actor bên trái là người dùng trực tiếp (khớp `D-07`); 2 khối bên phải là tác nhân/hệ thống phụ trợ (Email SMTP cho `FR-AUTH-04` và File CSV/Excel cho `FR-STU-04`, `FR-GRD-02`).
- **Tính nhất quán:** Không có hệ thống thanh toán hay cổng SSO ngoài phạm vi v1.0 (khớp Scope Document ở Giai đoạn 1).

---

## 2. USE CASE DIAGRAMS (MỤC 6.2 & 9.3.6)

### 2.1. Quy ước quan hệ UML Use Case sử dụng trong dự án
1. **Association (`-->`):** Kết nối giữa Actor và Use Case mà Actor đó trực tiếp khởi phát.
2. **Include (`..> <<include>>`):** Use Case cơ sở **luôn luôn bắt buộc** thực thi Use Case được include (ví dụ: `Đăng ký học phần` luôn `<<include>>` `Kiểm tra điều kiện đăng ký`; `Mở lớp học phần` luôn `<<include>>` `Kiểm tra trùng lịch Phòng & Giảng viên`).
3. **Extend (`<.. <<extend>>`):** Use Case mở rộng **chỉ thực thi có điều kiện** từ Use Case cơ sở (mũi tên trỏ từ Use Case mở rộng về Use Case gốc; ví dụ: `Import bảng điểm từ Excel` `<<extend>>` `Nhập điểm thành phần`).

---

### 2.2. Sơ đồ Use Case Tổng quan (Overall Use Case Diagram)

#### Mục đích & Phạm vi
Thể hiện bức tranh tổng thể các nhóm chức năng chính của toàn hệ thống và sự phân bổ cho 4 Actor trong ranh giới hệ thống (`System Boundary`).

#### Sơ đồ trực quan (Mermaid)
```mermaid
flowchart LR
    ADMIN["👤 Admin"]
    AA["👤 Phòng Đào tạo"]
    LEC["👤 Giảng viên"]
    STU["👤 Sinh viên"]

    subgraph SYS["HỆ THỐNG QUẢN LÝ ĐÀO TẠO SINH VIÊN PTIT"]
        UC_AUTH(["UC-G1: Đăng nhập, Đổi/Quên mật khẩu"])
        UC_RBAC(["UC-G2: Quản trị Tài khoản, RBAC & Audit Log"])
        UC_ORG(["UC-G3: Quản lý Khoa, Bộ môn, Ngành, Lớp HC, GV"])
        UC_STU(["UC-G4: Quản lý Hồ sơ & Trạng thái Sinh viên"])
        UC_CRS(["UC-G5: Quản lý Học phần, Tiên quyết & CTĐT"])
        UC_SEC(["UC-G6: Quản lý Học kỳ, Đợt ĐK & Lớp học phần"])
        UC_ENR(["UC-G7: Đăng ký & Hủy đăng ký Học phần"])
        UC_SCH(["UC-G8: Tra cứu Thời khóa biểu"])
        UC_GRD(["UC-G9: Nhập, Công bố, Khóa điểm & Tính GPA"])
        UC_RPT(["UC-G10: Xem Báo cáo & Thống kê đào tạo"])
    end

    ADMIN --> UC_AUTH
    ADMIN --> UC_RBAC
    ADMIN --> UC_RPT

    AA --> UC_AUTH
    AA --> UC_ORG
    AA --> UC_STU
    AA --> UC_CRS
    AA --> UC_SEC
    AA --> UC_ENR
    AA --> UC_GRD
    AA --> UC_RPT

    LEC --> UC_AUTH
    LEC --> UC_SCH
    LEC --> UC_GRD
    LEC --> UC_RPT

    STU --> UC_AUTH
    STU --> UC_STU
    STU --> UC_CRS
    STU --> UC_ENR
    STU --> UC_SCH
    STU --> UC_GRD
```

#### Mã nguồn PlantUML (Overall Use Case)
```plantuml
@startuml Overall_UseCase_Diagram
left to right direction
skinparam packageStyle rectangle

actor "Admin" as Admin
actor "Phòng Đào tạo" as AA
actor "Giảng viên" as Lecturer
actor "Sinh viên" as Student

rectangle "Hệ thống Quản lý Đào tạo Sinh viên PTIT" {
  usecase "UC-G1: Xác thực & Quản lý mật khẩu" as UCG1
  usecase "UC-G2: Quản lý Tài khoản, RBAC & Audit Log" as UCG2
  usecase "UC-G3: Quản lý Khoa, Bộ môn, Ngành, Lớp HC, GV" as UCG3
  usecase "UC-G4: Quản lý Hồ sơ & Trạng thái Sinh viên" as UCG4
  usecase "UC-G5: Quản lý Học phần, Tiên quyết & CTĐT" as UCG5
  usecase "UC-G6: Quản lý Học kỳ, Đợt ĐK & Lớp học phần" as UCG6
  usecase "UC-G7: Đăng ký & Hủy đăng ký Học phần" as UCG7
  usecase "UC-G8: Xem Thời khóa biểu" as UCG8
  usecase "UC-G9: Quản lý Điểm & Tra cứu Kết quả học tập" as UCG9
  usecase "UC-G10: Báo cáo & Thống kê đào tạo" as UCG10
}

Admin --> UCG1
Admin --> UCG2
Admin --> UCG10

AA --> UCG1
AA --> UCG3
AA --> UCG4
AA --> UCG5
AA --> UCG6
AA --> UCG7
AA --> UCG9
AA --> UCG10

Lecturer --> UCG1
Lecturer --> UCG8
Lecturer --> UCG9
Lecturer --> UCG10

Student --> UCG1
Student --> UCG4
Student --> UCG5
Student --> UCG7
Student --> UCG8
Student --> UCG9
@enduml
```

---

### 2.3. Use Case Chi tiết Phân hệ 1: Authentication & Authorization (Bắt buộc theo Mục 9.3.6)

#### Mục đích & Phạm vi
Mô tả chi tiết các ca sử dụng thuộc phân hệ Xác thực (Authentication) và Phân quyền RBAC / Kiểm toán (Authorization), bao gồm cả cơ chế thu hồi Refresh Token khi khóa tài khoản.

#### Sơ đồ trực quan (Mermaid)
```mermaid
flowchart LR
    USER["👤 Người dùng<br/>(Admin / PĐT / GV / SV)"]
    ADMIN["👤 Admin"]

    subgraph AUTH_BOUNDARY["Phân hệ Authentication & Authorization"]
        UC01(["UC-AUTH-01: Đăng nhập"])
        UC02(["UC-AUTH-02: Đăng xuất"])
        UC03(["UC-AUTH-03: Làm mới phiên (Refresh Token)"])
        UC04(["UC-AUTH-04: Đổi mật khẩu"])
        UC05(["UC-AUTH-05: Quên & Đặt lại mật khẩu"])
        UC06(["UC-AUTH-06: Kiểm tra trạng thái tài khoản ACTIVE"])
        UC07(["UC-AUTH-07: Thu hồi toàn bộ Refresh Token"])

        UC08(["UC-RBAC-01: Quản lý Tài khoản (Tạo/Sửa)"])
        UC09(["UC-RBAC-02: Khóa / Mở khóa Tài khoản"])
        UC10(["UC-RBAC-03: Quản lý Role & Permission"])
        UC11(["UC-RBAC-04: Gán Role cho User"])
        UC12(["UC-RBAC-05: Tra cứu Nhật ký kiểm toán (Audit Log)"])
        UC13(["UC-RBAC-06: Ghi nhận Audit Log"])
    end

    USER --> UC01
    USER --> UC02
    USER --> UC03
    USER --> UC04
    USER --> UC05

    ADMIN --> UC08
    ADMIN --> UC09
    ADMIN --> UC10
    ADMIN --> UC11
    ADMIN --> UC12

    UC01 -. "<<include>>" .-> UC06
    UC03 -. "<<include>>" .-> UC06
    UC04 -. "<<include>>" .-> UC07
    UC09 -. "<<include>>" .-> UC07
    UC09 -. "<<include>>" .-> UC13
    UC10 -. "<<include>>" .-> UC13
    UC11 -. "<<include>>" .-> UC13
```

#### Mã nguồn PlantUML (Authentication & Authorization Use Case)
```plantuml
@startuml Auth_RBAC_UseCase_Diagram
left to right direction
skinparam packageStyle rectangle

actor "Người dùng\n(Mọi vai trò)" as User
actor "Admin" as Admin
Admin --|> User

rectangle "Phân hệ Authentication & Authorization" {
  package "Authentication" {
    usecase "UC-AUTH-01: Đăng nhập" as UC_Login
    usecase "UC-AUTH-02: Đăng xuất" as UC_Logout
    usecase "UC-AUTH-03: Làm mới Access Token" as UC_Refresh
    usecase "UC-AUTH-04: Đổi mật khẩu" as UC_ChangePwd
    usecase "UC-AUTH-05: Quên & Đặt lại mật khẩu" as UC_ResetPwd
    usecase "UC-AUTH-06: Kiểm tra trạng thái tài khoản" as UC_CheckStatus
    usecase "UC-AUTH-07: Thu hồi toàn bộ Refresh Token" as UC_RevokeTokens
  }

  package "Authorization (RBAC) & Audit" {
    usecase "UC-RBAC-01: Quản lý Tài khoản" as UC_ManageUser
    usecase "UC-RBAC-02: Khóa / Mở khóa Tài khoản" as UC_LockUser
    usecase "UC-RBAC-03: Quản lý Role & Permission" as UC_ManageRole
    usecase "UC-RBAC-04: Gán Role cho User" as UC_AssignRole
    usecase "UC-RBAC-05: Tra cứu Audit Log" as UC_ViewAudit
    usecase "UC-RBAC-06: Ghi nhận Audit Log" as UC_WriteAudit
  }
}

User --> UC_Login
User --> UC_Logout
User --> UC_Refresh
User --> UC_ChangePwd
User --> UC_ResetPwd

Admin --> UC_ManageUser
Admin --> UC_LockUser
Admin --> UC_ManageRole
Admin --> UC_AssignRole
Admin --> UC_ViewAudit

UC_Login ..> UC_CheckStatus : <<include>>
UC_Refresh ..> UC_CheckStatus : <<include>>
UC_ChangePwd ..> UC_RevokeTokens : <<include>>
UC_ResetPwd ..> UC_RevokeTokens : <<include>>
UC_LockUser ..> UC_RevokeTokens : <<include>>
UC_LockUser ..> UC_WriteAudit : <<include>>
UC_ManageRole ..> UC_WriteAudit : <<include>>
UC_AssignRole ..> UC_WriteAudit : <<include>>
@enduml
```

#### Giải thích ký hiệu & Quan hệ
- **Generalization (`Admin --|> User`):** Admin kế thừa toàn bộ các Use Case xác thực của một Người dùng thông thường và có thêm các Use Case quản trị RBAC.
- **`<<include>>` `UC-AUTH-06`:** Cả lúc Đăng nhập và lúc Làm mới token đều bắt buộc kiểm tra tài khoản còn `ACTIVE` (`BR-02`).
- **`<<include>>` `UC-AUTH-07`:** Khi Đổi mật khẩu, Đặt lại mật khẩu hoặc khi Admin Khóa tài khoản, hệ thống luôn bắt buộc thu hồi mọi Refresh Token đang hoạt động của user đó.

---

### 2.4. Use Case Chi tiết Phân hệ 2: Tổ chức, Sinh viên, Học phần & Chương trình đào tạo

#### Mục đích & Phạm vi
Thể hiện các Use Case thiết lập dữ liệu gốc học vụ của Phòng đào tạo và các Use Case tra cứu/cập nhật hồ sơ của Sinh viên.

#### Sơ đồ trực quan (Mermaid)
```mermaid
flowchart LR
    AA["👤 Phòng Đào tạo"]
    STU["👤 Sinh viên"]

    subgraph ORG_STU_CRS["Phân hệ Tổ chức, Sinh viên, Học phần & CTĐT"]
        UC_O1(["UC-ORG-01: Quản lý Khoa, Bộ môn, Ngành, Lớp HC"])
        UC_O2(["UC-ORG-02: Quản lý Hồ sơ Giảng viên"])
        UC_S1(["UC-STU-01: Tạo & Cập nhật Hồ sơ Sinh viên"])
        UC_S2(["UC-STU-02: Import Sinh viên từ Excel"])
        UC_S3(["UC-STU-03: Đổi Trạng thái học vụ Sinh viên"])
        UC_S4(["UC-STU-04: Xem Hồ sơ & Tự sửa thông tin liên lạc"])
        UC_S5(["UC-STU-05: Khởi tạo Tài khoản User tự động"])

        UC_C1(["UC-CRS-01: Quản lý Học phần & Thành phần điểm"])
        UC_C2(["UC-CRS-02: Thiết lập Tiên quyết / Học trước"])
        UC_C3(["UC-CRS-03: Kiểm tra chống chu trình Tiên quyết"])
        UC_C4(["UC-CUR-01: Quản lý Chương trình đào tạo"])
        UC_C5(["UC-CUR-02: Xem CTĐT & Tiến độ hoàn thành"])
    end

    AA --> UC_O1
    AA --> UC_O2
    AA --> UC_S1
    AA --> UC_S3
    AA --> UC_C1
    AA --> UC_C2
    AA --> UC_C4
    AA --> UC_C5

    STU --> UC_S4
    STU --> UC_C5

    UC_O2 -. "<<include>>" .-> UC_S5
    UC_S1 -. "<<include>>" .-> UC_S5
    UC_S2 -. "<<extend>>" .-> UC_S1
    UC_C2 -. "<<include>>" .-> UC_C3
```

#### Mã nguồn PlantUML
```plantuml
@startuml Org_Student_Course_UseCase
left to right direction
skinparam packageStyle rectangle

actor "Phòng Đào tạo" as AA
actor "Sinh viên" as Student

rectangle "Phân hệ Tổ chức, Sinh viên, Học phần & CTĐT" {
  usecase "UC-ORG-01: Quản lý Khoa, Bộ môn, Ngành, Lớp HC" as UC_Org
  usecase "UC-ORG-02: Quản lý Hồ sơ Giảng viên" as UC_Lec
  usecase "UC-STU-01: Tạo & Cập nhật Hồ sơ Sinh viên" as UC_Stu
  usecase "UC-STU-02: Import Sinh viên từ Excel" as UC_ImportStu
  usecase "UC-STU-03: Đổi Trạng thái học vụ Sinh viên" as UC_StuStatus
  usecase "UC-STU-04: Xem Hồ sơ & Sửa thông tin liên lạc" as UC_StuSelf
  usecase "UC-STU-05: Khởi tạo Tài khoản User" as UC_CreateUser

  usecase "UC-CRS-01: Quản lý Học phần & Thành phần điểm" as UC_Course
  usecase "UC-CRS-02: Thiết lập Tiên quyết / Học trước" as UC_Prereq
  usecase "UC-CRS-03: Kiểm tra chống chu trình (DAG)" as UC_CheckCycle
  usecase "UC-CUR-01: Quản lý Chương trình đào tạo" as UC_Curr
  usecase "UC-CUR-02: Xem CTĐT & Tiến độ hoàn thành" as UC_ViewCurr
}

AA --> UC_Org
AA --> UC_Lec
AA --> UC_Stu
AA --> UC_StuStatus
AA --> UC_Course
AA --> UC_Prereq
AA --> UC_Curr
AA --> UC_ViewCurr

Student --> UC_StuSelf
Student --> UC_ViewCurr

UC_Lec ..> UC_CreateUser : <<include>>
UC_Stu ..> UC_CreateUser : <<include>>
UC_ImportStu .up.> UC_Stu : <<extend>>
UC_Prereq ..> UC_CheckCycle : <<include>>
@enduml
```

---

### 2.5. Use Case Chi tiết Phân hệ 3: Học kỳ, Lớp học phần, Thời khóa biểu & Đăng ký học phần

#### Mục đích & Phạm vi
Thể hiện các Use Case trọng tâm về lập kế hoạch học kỳ, mở lớp học phần có kiểm tra trùng lịch và đăng ký học phần.

#### Sơ đồ trực quan (Mermaid)
```mermaid
flowchart LR
    AA["👤 Phòng Đào tạo"]
    LEC["👤 Giảng viên"]
    STU["👤 Sinh viên"]

    subgraph SEC_ENR_BOUNDARY["Phân hệ Học kỳ, Lớp học phần, TKB & Đăng ký học phần"]
        UC_M1(["UC-SEM-01: Quản lý Năm học, Học kỳ & Giới hạn TC"])
        UC_M2(["UC-SEM-02: Cấu hình & Đóng/Mở Đợt đăng ký"])
        UC_M3(["UC-SEC-01: Quản lý Phòng học"])
        UC_M4(["UC-SEC-02: Mở Lớp học phần, Gán GV & Xếp lịch"])
        UC_M5(["UC-SEC-03: Kiểm tra trùng lịch Phòng & Giảng viên"])
        UC_M6(["UC-SEC-04: Tra cứu Lớp học phần mở trong kỳ"])

        UC_E1(["UC-ENR-01: Sinh viên tự Đăng ký học phần"])
        UC_E2(["UC-ENR-02: Sinh viên tự Hủy đăng ký học phần"])
        UC_E3(["UC-ENR-03: PĐT Đăng ký / Hủy học phần hộ"])
        UC_E4(["UC-ENR-04: Kiểm tra Tiên quyết, Tín chỉ, Trùng lịch & Sĩ số"])
        UC_E5(["UC-SCH-01: Xem Thời khóa biểu học tập / giảng dạy"])
    end

    AA --> UC_M1
    AA --> UC_M2
    AA --> UC_M3
    AA --> UC_M4
    AA --> UC_M6
    AA --> UC_E3

    LEC --> UC_M6
    LEC --> UC_E5

    STU --> UC_M6
    STU --> UC_E1
    STU --> UC_E2
    STU --> UC_E5

    UC_M4 -. "<<include>>" .-> UC_M5
    UC_E1 -. "<<include>>" .-> UC_E4
    UC_E3 -. "<<include>>" .-> UC_E4
```

#### Mã nguồn PlantUML
```plantuml
@startuml Section_Enrollment_UseCase
left to right direction
skinparam packageStyle rectangle

actor "Phòng Đào tạo" as AA
actor "Giảng viên" as Lecturer
actor "Sinh viên" as Student

rectangle "Phân hệ Học kỳ, Lớp học phần, TKB & Đăng ký học phần" {
  usecase "UC-SEM-01: Quản lý Năm học, Học kỳ & Giới hạn TC" as UC_Sem
  usecase "UC-SEM-02: Đóng/Mở Đợt đăng ký học phần" as UC_RegPeriod
  usecase "UC-SEC-01: Quản lý Phòng học" as UC_Room
  usecase "UC-SEC-02: Mở Lớp học phần, Gán GV & Xếp lịch" as UC_OpenSec
  usecase "UC-SEC-03: Kiểm tra trùng lịch Phòng & Giảng viên" as UC_CheckSecConflict
  usecase "UC-SEC-04: Tra cứu Lớp học phần mở" as UC_SearchSec

  usecase "UC-ENR-01: Sinh viên tự Đăng ký học phần" as UC_SelfEnroll
  usecase "UC-ENR-02: Sinh viên tự Hủy đăng ký" as UC_SelfCancel
  usecase "UC-ENR-03: PĐT Đăng ký / Hủy hộ (Override)" as UC_AdminEnroll
  usecase "UC-ENR-04: Kiểm tra Tiên quyết, Tín chỉ,\nTrùng lịch & Sĩ số (FOR UPDATE)" as UC_ValidateEnroll
  usecase "UC-SCH-01: Xem Thời khóa biểu" as UC_ViewSch
}

AA --> UC_Sem
AA --> UC_RegPeriod
AA --> UC_Room
AA --> UC_OpenSec
AA --> UC_SearchSec
AA --> UC_AdminEnroll

Lecturer --> UC_SearchSec
Lecturer --> UC_ViewSch

Student --> UC_SearchSec
Student --> UC_SelfEnroll
Student --> UC_SelfCancel
Student --> UC_ViewSch

UC_OpenSec ..> UC_CheckSecConflict : <<include>>
UC_SelfEnroll ..> UC_ValidateEnroll : <<include>>
UC_AdminEnroll ..> UC_ValidateEnroll : <<include>>
@enduml
```

---

### 2.6. Use Case Chi tiết Phân hệ 4: Quản lý Điểm, Kết quả học tập & Báo cáo

#### Mục đích & Phạm vi
Mô tả các Use Case nhập điểm, import điểm, tính tự động điểm tổng kết, công bố, khóa sổ, điều chỉnh đặc biệt và tra cứu kết quả / báo cáo.

#### Sơ đồ trực quan (Mermaid)
```mermaid
flowchart LR
    LEC["👤 Giảng viên"]
    AA["👤 Phòng Đào tạo"]
    STU["👤 Sinh viên"]
    ADMIN["👤 Admin"]

    subgraph GRD_RPT_BOUNDARY["Phân hệ Điểm, Kết quả học tập & Báo cáo"]
        UC_G1(["UC-GRD-01: Xem Danh sách SV Lớp học phần phụ trách"])
        UC_G2(["UC-GRD-02: Nhập/Sửa điểm thành phần (khi DRAFT)"])
        UC_G3(["UC-GRD-03: Import bảng điểm từ Excel"])
        UC_G4(["UC-GRD-04: Tính điểm tổng kết hệ 10, chữ, hệ 4 & xét liệt"])
        UC_G5(["UC-GRD-05: Công bố bảng điểm (DRAFT -> PUBLISHED)"])
        UC_G6(["UC-GRD-06: Khóa sổ điểm (PUBLISHED -> LOCKED)"])
        UC_G7(["UC-GRD-07: Sửa điểm sau công bố/khóa (Override + Audit)"])
        UC_G8(["UC-GRD-08: Tra cứu Điểm học kỳ, GPA & CPA tích lũy"])
        UC_R1(["UC-RPT-01: Xem Báo cáo & Thống kê đào tạo"])
    end

    LEC --> UC_G1
    LEC --> UC_G2
    LEC --> UC_G5
    LEC --> UC_R1

    AA --> UC_G1
    AA --> UC_G5
    AA --> UC_G6
    AA --> UC_G7
    AA --> UC_G8
    AA --> UC_R1

    STU --> UC_G8
    ADMIN --> UC_R1

    UC_G3 -. "<<extend>>" .-> UC_G2
    UC_G2 -. "<<include>>" .-> UC_G4
    UC_G7 -. "<<include>>" .-> UC_G4
```

#### Mã nguồn PlantUML
```plantuml
@startuml Grade_Report_UseCase
left to right direction
skinparam packageStyle rectangle

actor "Giảng viên" as Lecturer
actor "Phòng Đào tạo" as AA
actor "Sinh viên" as Student
actor "Admin" as Admin

rectangle "Phân hệ Điểm, Kết quả học tập & Báo cáo" {
  usecase "UC-GRD-01: Xem Danh sách SV Lớp học phần" as UC_ClassRoster
  usecase "UC-GRD-02: Nhập/Sửa điểm thành phần (DRAFT)" as UC_EnterGrade
  usecase "UC-GRD-03: Import bảng điểm từ Excel" as UC_ImportGrade
  usecase "UC-GRD-04: Tính điểm tổng kết & Xét điểm liệt" as UC_CalcGrade
  usecase "UC-GRD-05: Công bố bảng điểm (PUBLISHED)" as UC_PublishGrade
  usecase "UC-GRD-06: Khóa sổ điểm (LOCKED)" as UC_LockGrade
  usecase "UC-GRD-07: Sửa điểm sau khóa (Override + Audit)" as UC_OverrideGrade
  usecase "UC-GRD-08: Tra cứu Điểm, GPA học kỳ & CPA tích lũy" as UC_ViewTranscript
  usecase "UC-RPT-01: Xem Báo cáo & Thống kê đào tạo" as UC_Report
}

Lecturer --> UC_ClassRoster
Lecturer --> UC_EnterGrade
Lecturer --> UC_PublishGrade
Lecturer --> UC_Report

AA --> UC_ClassRoster
AA --> UC_PublishGrade
AA --> UC_LockGrade
AA --> UC_OverrideGrade
AA --> UC_ViewTranscript
AA --> UC_Report

Student --> UC_ViewTranscript
Admin --> UC_Report

UC_ImportGrade .up.> UC_EnterGrade : <<extend>>
UC_EnterGrade ..> UC_CalcGrade : <<include>>
UC_OverrideGrade ..> UC_CalcGrade : <<include>>
@enduml
```

---

## 3. USE CASE SPECIFICATIONS (ĐẶC TẢ CHI TIẾT CÁC USE CASE TRỌNG TÂM — MỤC 6.3)

### 3.1. `UC-AUTH-01` — Đăng nhập hệ thống
| Hạng mục | Nội dung |
|---|---|
| **Use Case ID** | `UC-AUTH-01` |
| **Use Case Name** | Đăng nhập hệ thống (User Login) |
| **Actor** | Admin, Phòng đào tạo, Giảng viên, Sinh viên |
| **Description** | Người dùng xác thực danh tính bằng tài khoản và mật khẩu để truy cập vào không gian làm việc tương ứng với vai trò của mình. |
| **Preconditions** | Người dùng đã có tài khoản trong bảng `users`. |
| **Trigger** | Người dùng nhập tài khoản, mật khẩu và bấm nút **"Đăng nhập"** trên màn hình `/login`. |
| **Main Flow** | 1. Người dùng nhập `usernameOrEmail` và `password`, nhấn "Đăng nhập".<br>2. Frontend kiểm tra không để trống và gửi `POST /api/v1/auth/login`.<br>3. Backend tra cứu bản ghi `User` theo `username` hoặc `email`.<br>4. Backend đối chiếu mật khẩu bằng `BCryptPasswordEncoder.matches()`.<br>5. Backend kiểm tra `user.status == 'ACTIVE'`.<br>6. Backend truy vấn danh sách `Role` và `Permission` của người dùng.<br>7. Backend sinh JWT `accessToken` (15 phút) và `refreshToken` (7 ngày, lưu hash SHA-256 vào bảng `refresh_tokens`), cập nhật `last_login_at`.<br>8. Backend trả về `HTTP 200 OK` kèm header `Set-Cookie` (`HttpOnly`) và thông tin người dùng.<br>9. Frontend điều hướng người dùng tới Dashboard tương ứng với vai trò (`/admin`, `/academic`, `/lecturer`, hoặc `/student`). |
| **Alternative Flow** | **AF-01 (Đã đăng nhập sẵn):** Nếu người dùng truy cập `/login` khi đã có `accessToken` hợp lệ, hệ thống tự động điều hướng thẳng vào Dashboard. |
| **Exception Flow** | **EF-01 (Không tồn tại tài khoản hoặc sai mật khẩu – tại bước 3, 4):** Hệ thống trả về `HTTP 401` với thông báo `"Tài khoản hoặc mật khẩu không chính xác"`.<br>**EF-02 (Tài khoản bị khóa – tại bước 5):** Nếu `user.status == 'LOCKED'` hoặc `'INACTIVE'`, hệ thống trả về `HTTP 403` với thông báo `"Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Quản trị viên"`. |
| **Postconditions** | **Thành công:** Một bản ghi `refresh_tokens` mới được lưu trong DB; trình duyệt giữ cookie `HttpOnly`.<br>**Thất bại:** Không có phiên nào được tạo. |
| **Business Rules** | `BR-01`, `BR-02`, `BR-03` |
| **Related Requirements** | `FR-AUTH-01`, `NFR-SEC-01` |

---

### 3.2. `UC-STU-01` — Quản lý Hồ sơ Sinh viên (Tạo mới & Import)
| Hạng mục | Nội dung |
|---|---|
| **Use Case ID** | `UC-STU-01` |
| **Use Case Name** | Tạo mới và Cập nhật Hồ sơ Sinh viên |
| **Actor** | Phòng Quản lý Đào tạo (`ACADEMIC_AFFAIRS`) |
| **Description** | Cán bộ Phòng đào tạo thêm mới hồ sơ sinh viên (từng cá nhân hoặc qua file Excel) và hệ thống tự động khởi tạo tài khoản đăng nhập cho sinh viên đó. |
| **Preconditions** | Cán bộ Phòng đào tạo đã đăng nhập và có quyền `STUDENT_WRITE`; danh mục Khoa, Ngành, Lớp hành chính đã tồn tại. |
| **Trigger** | Cán bộ chọn **"Thêm sinh viên"** hoặc **"Import Excel"** tại màn hình Quản lý sinh viên. |
| **Main Flow** | 1. Cán bộ nhập thông tin sinh viên (`studentCode`, `fullName`, `dateOfBirth`, `gender`, `nationalId`, `email`, `academicClassId`, `majorId`, `cohortYear`) và bấm "Lưu".<br>2. Hệ thống kiểm tra định dạng dữ liệu và kiểm tra không trùng `studentCode`, `email`, `nationalId`.<br>3. Hệ thống kiểm tra `academicClassId` thuộc đúng `majorId`.<br>4. Hệ thống mở Database Transaction:<br>   - Tạo bản ghi `users` với `username = studentCode`, `email`, mật khẩu mặc định băm `BCrypt`, `status = 'ACTIVE'`.<br>   - Gán Role `STUDENT` vào `user_roles`.<br>   - Tạo bản ghi `students` liên kết `user_id`, `academic_status = 'ACTIVE'`.<br>   - Ghi bản ghi khởi tạo vào `student_status_history`.<br>5. Hệ thống commit Transaction và trả về thông tin hồ sơ sinh viên vừa tạo (`HTTP 201`). |
| **Alternative Flow** | **AF-01 (Import từ file Excel – `UC-STU-02`):**<br>1a. Cán bộ tải file `.xlsx` lên.<br>2a. Hệ thống đọc toàn bộ dòng, kiểm tra hợp lệ từng dòng và trả về màn hình Preview.<br>3a. Nếu 0 lỗi và cán bộ bấm "Xác nhận Import", hệ thống thực hiện bước 4 cho toàn bộ danh sách trong 1 Transaction duy nhất. |
| **Exception Flow** | **EF-01 (Trùng Mã SV / Email / CCCD):** Rollback Transaction, trả về `HTTP 409 CONFLICT` chỉ rõ trường bị trùng.<br>**EF-02 (File Import có dòng lỗi):** Không ghi dòng nào vào DB, trả về danh sách dòng lỗi kèm nguyên nhân (`BR-32`). |
| **Postconditions** | Hồ sơ `Student` và tài khoản `User` tương ứng được lưu nhất quán trong DB. |
| **Business Rules** | `BR-04`, `BR-05`, `BR-06`, `BR-08`, `BR-32` |
| **Related Requirements** | `FR-STU-01`, `FR-STU-02`, `FR-STU-04` |

---

### 3.3. `UC-SEC-02` — Mở Lớp học phần, Phân công Giảng viên & Xếp lịch học
| Hạng mục | Nội dung |
|---|---|
| **Use Case ID** | `UC-SEC-02` |
| **Use Case Name** | Mở Lớp học phần, Phân công Giảng viên và Xếp lịch học |
| **Actor** | Phòng Quản lý Đào tạo (`ACADEMIC_AFFAIRS`) |
| **Description** | Mở lớp học phần trong một học kỳ, gán giảng viên dạy và xếp lịch học theo phòng – thứ – tiết – tuần có kiểm tra chống xung đột. |
| **Preconditions** | Học kỳ, Học phần, Giảng viên và Phòng học đã được tạo và ở trạng thái `ACTIVE`. |
| **Trigger** | Cán bộ Phòng đào tạo chọn **"Mở lớp học phần"** trong màn hình Quản lý lớp học phần. |
| **Main Flow** | 1. Cán bộ chọn Học kỳ, Học phần, nhập Số nhóm (`groupNumber`), Sĩ số tối đa (`maxCapacity`), chọn Giảng viên phụ trách và thêm một hoặc nhiều dòng lịch học `(dayOfWeek, startPeriod, endPeriod, startWeek, endWeek, classroomId)`.<br>2. Hệ thống kiểm tra cặp `(courseId, semesterId, groupNumber)` chưa tồn tại.<br>3. Hệ thống kiểm tra `maxCapacity <= classroom.capacity` với mọi phòng học được chọn.<br>4. Hệ thống kiểm tra **trùng lịch Phòng học (`BR-16`)**: không có lớp học phần nào khác đang mở trong cùng học kỳ sử dụng cùng `classroomId` có cùng `dayOfWeek`, giao đoạn tuần và giao đoạn tiết.<br>5. Hệ thống kiểm tra **trùng lịch Giảng viên (`BR-17`)**: giảng viên được chọn không dạy lớp nào khác trong cùng học kỳ bị trùng `dayOfWeek`, giao đoạn tuần và giao đoạn tiết.<br>6. Hệ thống lưu `course_sections` (`status = 'OPEN'`, `grade_status = 'DRAFT'`, `current_enrolled = 0`) và các dòng `section_schedules` trong 1 Transaction.<br>7. Hệ thống trả về thông tin lớp học phần vừa mở (`HTTP 201`). |
| **Alternative Flow** | **AF-01 (Chưa phân công Giảng viên ngay lúc mở lớp):** Cán bộ có thể để trống `lecturerId` khi tạo lớp và phân công bổ sung trước khi học kỳ bắt đầu. |
| **Exception Flow** | **EF-01 (Vượt sức chứa phòng học):** Trả `HTTP 400 CLASSROOM_CAPACITY_EXCEEDED`.<br>**EF-02 (Trùng lịch Phòng học):** Trả `HTTP 409 SCHEDULE_CLASSROOM_CONFLICT` kèm mã lớp học phần đang chiếm phòng.<br>**EF-03 (Trùng lịch Giảng viên):** Trả `HTTP 409 SCHEDULE_LECTURER_CONFLICT` kèm mã lớp học phần giảng viên đang dạy. |
| **Postconditions** | Lớp học phần và lịch học được tạo, đảm bảo không có xung đột lịch phòng và lịch giảng viên. |
| **Business Rules** | `BR-14`, `BR-15`, `BR-16`, `BR-17`, `BR-18` |
| **Related Requirements** | `FR-SEC-01`, `FR-SEC-02` |

---

### 3.4. `UC-ENR-01` — Sinh viên tự Đăng ký học phần (Trọng tâm Transaction & Concurrency)
| Hạng mục | Nội dung |
|---|---|
| **Use Case ID** | `UC-ENR-01` |
| **Use Case Name** | Sinh viên tự Đăng ký Lớp học phần |
| **Actor** | Sinh viên (`STUDENT`) |
| **Description** | Sinh viên chọn đăng ký một lớp học phần đang mở trong đợt đăng ký của học kỳ hiện hành. Hệ thống kiểm tra toàn bộ điều kiện học vụ và xử lý đồng thời an toàn. |
| **Preconditions** | Sinh viên đã đăng nhập, có Role `STUDENT`. |
| **Trigger** | Sinh viên bấm nút **"Đăng ký"** tại một lớp học phần trên màn hình Đăng ký học phần. |
| **Main Flow** | 1. Frontend gửi `POST /api/v1/enrollments` với `{ sectionId }`.<br>2. Backend mở `@Transactional` và lấy hồ sơ `Student` của người dùng đang đăng nhập.<br>3. Kiểm tra `student.academic_status == 'ACTIVE'` (`BR-19`).<br>4. Thực thi `SELECT ... FROM course_sections WHERE id = :sectionId FOR UPDATE` để khóa dòng lớp học phần (`BR-25`).<br>5. Kiểm tra `section.status == 'OPEN'` và đợt đăng ký của học kỳ đang mở (`semester.is_registration_open == true` và `NOW()` thuộc `[registration_start_at, registration_end_at]`, `BR-20`).<br>6. Kiểm tra sĩ số: `section.current_enrolled < section.max_capacity` (`BR-25`).<br>7. Kiểm tra sinh viên chưa có bản ghi `ENROLLED` nào của cùng `course_id` trong cùng `semester_id` (`BR-21`).<br>8. Kiểm tra điều kiện Tiên quyết (`PREREQUISITE`: đã qua môn ở kỳ trước với `is_passed = true`) và Học trước (`COREQUISITE`: đã có điểm tổng kết ở kỳ trước) của `section.course_id` (`BR-22`).<br>9. Kiểm tra tổng số tín chỉ đã đăng ký trong kỳ + `course.credits <= semester.max_credits` (`BR-23`).<br>10. Kiểm tra lịch học của `sectionId` không trùng với bất kỳ lớp nào sinh viên đang `ENROLLED` trong kỳ (`BR-24`).<br>11. Lưu bản ghi `enrollments` (`status = 'ENROLLED'`), cập nhật `course_sections.current_enrolled = current_enrolled + 1`, ghi log và **Commit Transaction**.<br>12. Trả kết quả đăng ký thành công (`HTTP 201`) để Frontend cập nhật danh sách đã đăng ký và sĩ số lớp. |
| **Alternative Flow** | **AF-01 (Đăng ký học lại / học cải thiện):** Nếu sinh viên đã từng học môn này ở học kỳ trước (đã trượt `F` hoặc đã qua nhưng muốn cải thiện điểm), hệ thống tính `attempt_number = số_lần_trước + 1` và cho phép đăng ký bình thường (`BR-27`).<br>**AF-02 (Đăng ký lại lớp vừa hủy trong cùng kỳ):** Nếu sinh viên đã đăng ký rồi hủy chính lớp đó trước đó (`status = 'CANCELLED'`), hệ thống chuyển bản ghi về `ENROLLED` (sau khi vượt qua đầy đủ bước 3 → 10) và tăng `current_enrolled` thêm 1. |
| **Exception Flow** | **EF-01 (`403 ENROLL_STUDENT_NOT_ACTIVE`):** Sinh viên đang bảo lưu, đình chỉ, thôi học hoặc đã tốt nghiệp.<br>**EF-02 (`400 ENROLL_PERIOD_CLOSED`):** Ngoài thời gian đợt đăng ký hoặc đợt đăng ký đã đóng.<br>**EF-03 (`409 ENROLL_SECTION_FULL`):** Lớp đã đủ sĩ số (bao gồm trường hợp thua cuộc khi tranh suất cuối cùng).<br>**EF-04 (`409 ENROLL_DUPLICATE_COURSE_IN_SEMESTER`):** Đã đăng ký một nhóm lớp khác của cùng môn học trong kỳ.<br>**EF-05 (`400 ENROLL_PREREQUISITE_NOT_MET`):** Chưa đạt môn tiên quyết hoặc chưa học môn học trước.<br>**EF-06 (`400 ENROLL_MAX_CREDITS_EXCEEDED`):** Vượt quá số tín chỉ tối đa của học kỳ.<br>**EF-07 (`409 ENROLL_SCHEDULE_CONFLICT`):** Trùng thứ/tiết/tuần với một lớp đã đăng ký.<br>*Mọi Exception Flow đều tự động Rollback Transaction.* |
| **Postconditions** | Bản ghi `Enrollment` ở trạng thái `ENROLLED`; `current_enrolled` tăng đúng 1; không bao giờ vượt `max_capacity`. |
| **Business Rules** | `BR-15`, `BR-19` → `BR-27` |
| **Related Requirements** | `FR-ENR-01`, `NFR-INT-01`, `NFR-PERF-02` |

---

### 3.5. `UC-ENR-02` — Sinh viên tự Hủy đăng ký học phần
| Hạng mục | Nội dung |
|---|---|
| **Use Case ID** | `UC-ENR-02` |
| **Use Case Name** | Sinh viên tự Hủy đăng ký Lớp học phần |
| **Actor** | Sinh viên (`STUDENT`) |
| **Description** | Sinh viên rút khỏi một lớp học phần đã đăng ký khi đợt đăng ký vẫn đang mở. |
| **Preconditions** | Sinh viên đang có bản ghi `Enrollment` ở trạng thái `ENROLLED`. |
| **Trigger** | Sinh viên bấm **"Hủy đăng ký"** tại môn học trong danh sách học phần đã đăng ký. |
| **Main Flow** | 1. Sinh viên xác nhận hủy trên hộp thoại cảnh báo.<br>2. Backend mở `@Transactional`, kiểm tra bản ghi `Enrollment` thuộc về chính sinh viên đang đăng nhập và có `status == 'ENROLLED'`.<br>3. Kiểm tra đợt đăng ký của học kỳ đang mở (`BR-20`).<br>4. Khóa dòng `CourseSection` (`FOR UPDATE`).<br>5. Cập nhật `enrollment.status = 'CANCELLED'`, `cancelled_at = NOW()`, `cancelled_by = currentUserId`.<br>6. Giảm `course_sections.current_enrolled = current_enrolled - 1`.<br>7. Commit Transaction và trả về `HTTP 200 OK`. |
| **Alternative Flow** | Không có. |
| **Exception Flow** | **EF-01 (`400 ENROLL_PERIOD_CLOSED`):** Đợt đăng ký đã kết thúc; thông báo sinh viên liên hệ Phòng đào tạo nếu muốn rút học phần đặc biệt.<br>**EF-02 (`403 FORBIDDEN`):** Cố tình truyền `enrollmentId` của sinh viên khác. |
| **Postconditions** | Bản ghi `Enrollment` chuyển sang `CANCELLED` (không xóa vật lý); lớp học phần có thêm 1 suất trống. |
| **Business Rules** | `BR-03`, `BR-20`, `BR-26` |
| **Related Requirements** | `FR-ENR-02` |

---

### 3.6. `UC-GRD-02` & `UC-GRD-05` — Nhập, Tính tự động và Công bố Điểm lớp học phần
| Hạng mục | Nội dung |
|---|---|
| **Use Case ID** | `UC-GRD-02` & `UC-GRD-05` |
| **Use Case Name** | Nhập điểm thành phần, Tính điểm tổng kết và Công bố bảng điểm |
| **Actor** | Giảng viên (`LECTURER`) |
| **Description** | Giảng viên nhập điểm các thành phần cho sinh viên trong lớp học phần mình dạy, hệ thống tự động tính điểm tổng kết và giảng viên công bố bảng điểm. |
| **Preconditions** | Giảng viên đã đăng nhập, được phân công phụ trách lớp học phần (`section.lecturer_id == currentLecturerId`) và lớp đang có `grade_status == 'DRAFT'`. |
| **Trigger** | Giảng viên bấm **"Lưu bảng điểm"** hoặc **"Công bố điểm"** tại màn hình Quản lý điểm lớp học phần. |
| **Main Flow** | 1. Giảng viên mở lớp học phần được phân công; hệ thống hiển thị danh sách sinh viên `ENROLLED` và các cột thành phần điểm của môn học (ví dụ: Chuyên cần 10%, KT/TH 20%, Giữa kỳ 20%, Cuối kỳ 50%).<br>2. Giảng viên nhập/sửa điểm trên lưới (hoặc upload file Excel qua `UC-GRD-03`) và bấm **"Lưu nháp"**.<br>3. Backend kiểm tra quyền phụ trách lớp (`BR-03`), trạng thái `grade_status == 'DRAFT'` (`BR-30`) và mọi điểm nằm trong `[0.0, 10.0]` (`BR-28`).<br>4. Backend lưu vào `student_grades`. Với những sinh viên đã có đủ tất cả đầu điểm thành phần, hệ thống tự động tính `final_score_10`, kiểm tra điểm liệt cuối kỳ (`< 4.0`), quy đổi `letter_grade`, `gpa_score_4` và `is_passed` (`BR-29`).<br>5. Khi đã nhập đủ điểm cho 100% sinh viên trong lớp, Giảng viên bấm **"Công bố điểm"**.<br>6. Backend kiểm tra không còn sinh viên nào thiếu đầu điểm, chuyển `section.grade_status = 'PUBLISHED'`, `published_at = NOW()`, ghi `AuditLog` và trả kết quả thành công. |
| **Alternative Flow** | **AF-01 (Lưu từng phần):** Giảng viên có thể nhập điểm Chuyên cần và Giữa kỳ trước, lưu nháp nhiều lần trước khi nhập điểm Cuối kỳ và Công bố. |
| **Exception Flow** | **EF-01 (`403 GRADE_NOT_ASSIGNED_LECTURER`):** Giảng viên không phải người được phân công dạy lớp này.<br>**EF-02 (`400 GRADE_ALREADY_PUBLISHED_OR_LOCKED`):** Bảng điểm đã ở trạng thái `PUBLISHED` hoặc `LOCKED`.<br>**EF-03 (`400 GRADE_INCOMPLETE_FOR_PUBLISH`):** Bấm Công bố khi vẫn còn sinh viên trong lớp chưa có đủ đầu điểm. |
| **Postconditions** | Điểm thành phần và điểm tổng kết được lưu chính xác; khi chuyển sang `PUBLISHED`, sinh viên xem được điểm và giảng viên không tự sửa được nữa. |
| **Business Rules** | `BR-03`, `BR-28`, `BR-29`, `BR-30`, `BR-32` |
| **Related Requirements** | `FR-GRD-01`, `FR-GRD-02`, `FR-GRD-03`, `FR-GRD-04` |
