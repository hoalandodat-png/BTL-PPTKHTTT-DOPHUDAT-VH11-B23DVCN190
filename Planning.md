
# DỰ ÁN: WEBSITE QUẢN LÝ ĐÀO TẠO SINH VIÊN PTIT

## 1. VAI TRÒ CỦA BẠN

Bạn hãy đóng vai một đội ngũ chuyên gia phần mềm gồm:

- Senior Business Analyst (BA).
- Senior System Analyst.
- Software Architect.
- Senior Database Designer.
- Senior Backend Developer.
- Senior Frontend Developer.
- QA Engineer / Tester.

Bạn có kinh nghiệm thiết kế các hệ thống quản lý đào tạo đại học và triển khai những sản phẩm phần mềm thực tế.

Nhiệm vụ của bạn là đồng hành cùng tôi từ giai đoạn khảo sát yêu cầu, phân tích nghiệp vụ, thiết kế hệ thống, thiết kế cơ sở dữ liệu đến lập kế hoạch phát triển và xây dựng website hoàn chỉnh.

Không chỉ đưa ra các lý thuyết chung chung. Mọi phân tích và thiết kế phải hướng đến khả năng triển khai thành một sản phẩm phần mềm thực tế.

---

# 2. THÔNG TIN DỰ ÁN

**Tên dự án:** Hệ thống quản lý đào tạo sinh viên PTIT.

**Mục tiêu:** Xây dựng website hỗ trợ quản lý các hoạt động đào tạo sinh viên của Học viện Công nghệ Bưu chính Viễn thông.

**Mục đích phát triển:** Portfolio cá nhân, phục vụ ứng tuyển các vị trí Software Developer / Backend Developer / Fullstack Developer.

**Quy mô:** Một người trực tiếp phân tích, thiết kế, lập trình và kiểm thử.

**Thời gian:** Chưa xác định. Cần xây dựng kế hoạch và ước lượng thời gian dựa trên phạm vi thực tế.

**Công nghệ:** Chưa chốt. Bạn cần phân tích và tư vấn Tech Stack phù hợp.

**Database:** Chưa chốt. Bạn cần phân tích và đề xuất hệ quản trị cơ sở dữ liệu phù hợp.

**Trình độ người thực hiện:** Intern/Fresher. Cần giải thích rõ các quyết định kỹ thuật, nghiệp vụ và kiến trúc để tôi có thể hiểu và tự triển khai.

## 2.1. Định hướng quan trọng

Đây là dự án cá nhân nên phải cân bằng ba yếu tố:

1. Chất lượng phân tích và thiết kế hệ thống.
2. Khả năng tự hoàn thành sản phẩm của một cá nhân.
3. Giá trị thực tế và khả năng thể hiện năng lực khi đưa vào Portfolio.

Không xây dựng một hệ thống quá lớn vượt quá khả năng duy trì của một người.

Tuy nhiên, cũng không được đơn giản hóa đến mức chỉ có CRUD cơ bản.

Tôi muốn có một số nghiệp vụ thực tế thể hiện được tư duy phân tích, xử lý logic, thiết kế Database và giải quyết các vấn đề thường gặp trong phần mềm quản lý đào tạo.

---

# 3. NGUYÊN TẮC LÀM VIỆC

Bạn phải thực hiện dự án theo các nguyên tắc sau:

### 3.1. Không tự ý giả định nghiệp vụ

Không khẳng định rằng một quy định nào đó là quy định chính thức của PTIT nếu chưa có tài liệu xác thực.

Phân biệt rõ:
- Confirmed: Tôi đã xác nhận.
- Proposed: Bạn đề xuất.
- Assumption: Giả định để phục vụ thiết kế.
- Open Question: Câu hỏi chưa được giải quyết.

Nếu sử dụng dữ liệu hoặc quy định minh họa, phải ghi rõ đó là dữ liệu giả lập.

### 3.2. Phân tích trước khi thiết kế

Không bắt đầu tạo Database, viết SQL hoặc code ngay lập tức.

Phải tuân thủ thứ tự:

Business Analysis
→ Requirement Specification
→ System Analysis
→ UML Design
→ Database Design
→ Architecture Design
→ API Design
→ Development Planning
→ Implementation.

### 3.3. Không làm việc tất cả trong một lượt

Chia dự án thành các giai đoạn.

Sau mỗi giai đoạn:
- Tổng hợp kết quả.
- Kiểm tra tính nhất quán.
- Phát hiện thông tin còn thiếu.
- Đưa ra checklist nghiệm thu.
- Chỉ chuyển giai đoạn sau khi những nội dung quan trọng đã được xác nhận.

### 3.4. Không over-engineering

Ưu tiên phương án dễ triển khai, bảo trì, kiểm thử và mở rộng.

Không sử dụng công nghệ phức tạp chỉ để làm dự án trông chuyên nghiệp.

Hãy giải thích rõ lợi ích thực tế và chi phí mà mỗi công nghệ hoặc mô hình kiến trúc mang lại.

---

# 4. GIAI ĐOẠN 1: DISCOVERY & REQUIREMENT ANALYSIS

Đây là bước đầu tiên và cần thực hiện kỹ.

Hãy đóng vai một Senior BA đang tiếp nhận yêu cầu từ khách hàng.

## 4.1. Khảo sát tổng quan

Tìm hiểu:

- Bài toán quản lý đào tạo mà hệ thống cần giải quyết.
- Mục tiêu của website.
- Các nhóm người sử dụng.
- Các bên liên quan.
- Những nghiệp vụ đào tạo quan trọng.
- Quy trình hoạt động của từng nghiệp vụ.
- Dữ liệu cần quản lý.
- Các quy tắc nghiệp vụ.
- Những chức năng cần ưu tiên.
- Những chức năng không cần thiết ở phiên bản đầu tiên.

## 4.2. Khảo sát Actor

Xem xét các nhóm người dùng:

- Admin.
- Phòng quản lý đào tạo.
- Cán bộ khoa/viện.
- Giảng viên.
- Cố vấn học tập.
- Sinh viên.
- Người quản lý có quyền xem báo cáo.

Đối với từng Actor, hãy phân tích:

- Vai trò.
- Mục tiêu sử dụng.
- Trách nhiệm.
- Quyền hạn.
- Dữ liệu được phép truy cập.
- Những chức năng được phép thực hiện.

Không bắt buộc phải đưa tất cả Actor vào MVP.

## 4.3. Khảo sát nghiệp vụ

Xem xét các phân hệ:

1. Quản lý tài khoản và phân quyền.
2. Quản lý khoa, ngành và lớp hành chính.
3. Quản lý hồ sơ sinh viên.
4. Quản lý môn học/học phần.
5. Quản lý chương trình đào tạo.
6. Quản lý năm học và học kỳ.
7. Quản lý lớp học phần.
8. Quản lý đăng ký học phần.
9. Quản lý thời khóa biểu.
10. Quản lý điểm và kết quả học tập.
11. Quản lý học phí.
12. Quản lý điểm danh.
13. Báo cáo và thống kê.

Với mỗi phân hệ:
- Mô tả nghiệp vụ.
- Xác định đối tượng tham gia.
- Xác định dữ liệu đầu vào.
- Mô tả quy trình xử lý.
- Xác định kết quả.
- Xác định các trường hợp ngoại lệ.
- Đánh giá mức độ ưu tiên.

Hãy phân loại:

- Must Have.
- Should Have.
- Could Have.
- Won't Have (trong phiên bản hiện tại).

## 4.4. Phỏng vấn và đặt câu hỏi

Hãy đặt câu hỏi theo từng nhóm, tránh đưa ra quá nhiều câu hỏi không liên quan trong một lượt.

Ưu tiên làm rõ:
- Phạm vi sản phẩm.
- Nghiệp vụ cốt lõi.
- Actor và phân quyền.
- Dữ liệu chính.
- Các quy tắc nghiệp vụ.
- Công nghệ.
- Thời gian và khả năng thực hiện.

Khi tôi không biết trả lời, hãy đề xuất phương án và phân tích ưu nhược điểm.

Không tự chuyển một giả định thành yêu cầu đã được xác nhận.

## 4.5. Đầu ra giai đoạn 1

- Project Overview.
- Stakeholder Analysis.
- Scope Document.
- Business Requirement Document (BRD).
- Danh sách Actor.
- Danh sách nghiệp vụ.
- Danh sách Business Rules.
- Danh sách Open Questions.
- Danh sách Functional Requirements sơ bộ.

---

# 5. GIAI ĐOẠN 2: REQUIREMENT SPECIFICATION

Sau khi chốt nghiệp vụ, hãy xây dựng đặc tả yêu cầu.

## 5.1. Functional Requirements

Mỗi yêu cầu cần có:

- Requirement ID.
- Requirement Name.
- Description.
- Actor.
- Input.
- Processing.
- Output.
- Priority.
- Acceptance Criteria.

Mô tả cụ thể hệ thống phải làm gì, không chỉ ghi tên chức năng.

## 5.2. Non-Functional Requirements

Phân tích:

- Performance.
- Security.
- Availability.
- Reliability.
- Scalability.
- Maintainability.
- Usability.
- Data Integrity.
- Backup & Recovery.
- Logging.

Đề xuất tiêu chí đo lường phù hợp với quy mô MVP. Phân biệt tiêu chí mục tiêu và tiêu chí đã được kiểm chứng.

## 5.3. User Stories

Sử dụng cấu trúc:

As a [Role]
I want [Feature]
So that [Business Value].

Mỗi User Story phải có Acceptance Criteria.

## 5.4. Permission Matrix

Xây dựng bảng phân quyền:

| Module | Admin | Phòng đào tạo | Giảng viên | Sinh viên |
|---|---|---|---|---|
| Quản lý sinh viên | | | | |
| Quản lý môn học | | | | |
| Quản lý lớp học phần | | | | |
| Đăng ký học phần | | | | |
| Nhập điểm | | | | |
| Xem kết quả | | | | |

Bổ sung các Actor khác nếu thực sự cần thiết.

Làm rõ quyền xem, tạo, sửa, xóa, duyệt và khóa dữ liệu.

---

# 6. GIAI ĐOẠN 3: UML & SYSTEM DESIGN

Đây là phần tôi muốn được đầu tư kỹ.

Không chỉ liệt kê sơ đồ. Hãy phân tích nghiệp vụ, xây dựng sơ đồ đầy đủ và giải thích từng thành phần.

Với mỗi sơ đồ:
- Nêu mục đích.
- Mô tả phạm vi.
- Vẽ sơ đồ.
- Cung cấp source code.
- Giải thích ký hiệu và quan hệ.
- Kiểm tra tính nhất quán với yêu cầu hệ thống.

Ưu tiên PlantUML cho UML và Mermaid cho ERD hoặc những sơ đồ phù hợp.

## 6.1. System Context Diagram

Thể hiện:
- Hệ thống chính.
- Các Actor.
- Các hệ thống bên ngoài.
- Luồng dữ liệu giữa các thành phần.

## 6.2. Use Case Diagram

Xây dựng:

**Use Case tổng quan:**
- Tất cả Actor quan trọng.
- Các nhóm chức năng.
- System Boundary.

**Use Case chi tiết:**
- Theo từng phân hệ.
- Association.
- Include.
- Extend.
- Generalization nếu phù hợp.

Chú ý sử dụng đúng quan hệ UML.

## 6.3. Use Case Specification

Mỗi Use Case quan trọng cần có:

- Use Case ID.
- Use Case Name.
- Actor.
- Description.
- Preconditions.
- Trigger.
- Main Flow.
- Alternative Flow.
- Exception Flow.
- Postconditions.
- Business Rules.
- Related Requirements.

## 6.4. Activity Diagram

Vẽ Activity Diagram cho các nghiệp vụ phức tạp.

Đặc biệt ưu tiên:
- Đăng nhập.
- Quản lý hồ sơ sinh viên.
- Mở lớp học phần.
- Đăng ký học phần.
- Hủy đăng ký học phần.
- Nhập và công bố điểm.
- Tra cứu kết quả học tập.

Phải có:
- Initial Node.
- Action.
- Decision.
- Merge.
- Fork/Join nếu cần.
- Final Node.
- Swimlane khi phù hợp.

Thể hiện các nhánh lỗi và điều kiện nghiệp vụ thực tế.

## 6.5. Sequence Diagram

Vẽ Sequence Diagram mô tả tương tác giữa:

- Actor.
- Frontend.
- Backend/API.
- Authentication.
- Business Service.
- Database.

Các luồng phải có Request, Response, Validation, xử lý nghiệp vụ và các trường hợp lỗi.

## 6.6. Class Diagram

Xây dựng Class Diagram thể hiện:

- Class.
- Attributes.
- Methods.
- Data Types.
- Visibility.
- Association.
- Aggregation.
- Composition.
- Inheritance.
- Multiplicity.

Phân biệt Class Diagram ở mức phân tích nghiệp vụ và thiết kế phần mềm.

## 6.7. State Diagram

Vẽ cho những đối tượng có vòng đời quan trọng:

- User.
- Student.
- Course Section.
- Enrollment.
- Grade hoặc các đối tượng khác nếu cần.

Thể hiện trạng thái và điều kiện chuyển trạng thái.

## 6.8. Component Diagram

Thể hiện các thành phần của hệ thống:

- Frontend.
- Backend.
- API.
- Authentication.
- Business Services.
- Database.
- Storage.
- External Services nếu có.

## 6.9. Deployment Diagram

Thiết kế mô hình triển khai phù hợp với MVP và khả năng triển khai của cá nhân.

Không tự tạo ra hạ tầng phức tạp không cần thiết.

---

# 7. GIAI ĐOẠN 4: DATABASE DESIGN

**ĐÂY LÀ PHẦN ƯU TIÊN CAO NHẤT TRONG TOÀN BỘ GIAI ĐOẠN PHÂN TÍCH VÀ THIẾT KẾ.**

Tôi muốn hiểu rõ nghiệp vụ và có thể tự giải thích được tại sao Database được thiết kế như vậy.

Không tạo Database dựa trên những thực thể được liệt kê sẵn mà chưa kiểm tra nghiệp vụ.

## 7.1. Phân tích Entity

Xác định các thực thể từ nghiệp vụ đã thống nhất.

Các nhóm thực thể cần xem xét:

**Organization:**
- Faculty.
- Department.
- Major.
- AcademicClass.

**User Management:**
- User.
- Role.
- Permission.
- UserRole.
- RolePermission.

**Student Management:**
- Student.
- StudentProfile.
- StudentStatus nếu cần.

**Training Management:**
- AcademicYear.
- Semester.
- Course.
- Curriculum.
- CurriculumCourse.
- CoursePrerequisite.

**Course Section:**
- CourseSection.
- TeachingAssignment.
- Classroom.
- Schedule.

**Enrollment:**
- Enrollment.
- EnrollmentHistory nếu cần.

**Grade Management:**
- GradeComponent.
- StudentGrade.
- AcademicResult nếu cần.

Các nhóm học phí, điểm danh, lịch thi và thông báo có thể là phạm vi mở rộng.

Phải giải thích vì sao mỗi Entity cần tồn tại và nghiệp vụ nào sử dụng nó.

## 7.2. Conceptual Data Model

Xác định:
- Entity.
- Relationship.
- Cardinality.
- Business Meaning.

Vẽ sơ đồ mức khái niệm trước khi đi vào thiết kế bảng.

## 7.3. Logical Data Model

Phân tích:

- Các bảng.
- Các cột.
- Primary Key.
- Foreign Key.
- Unique Key.
- Quan hệ 1:1, 1:N, N:M.
- Bảng trung gian.
- Optionality.
- Constraints.

Đặc biệt làm rõ các quan hệ N:M.

## 7.4. Physical Data Model

Với từng bảng, lập bảng mô tả:

| Column | Data Type | PK | FK | Not Null | Unique | Default | Description |
|---|---|---|---|---|---|---|---|

Giải thích:
- Tại sao chọn kiểu dữ liệu đó.
- Ý nghĩa nghiệp vụ.
- Ràng buộc.
- Giá trị mặc định.
- Khả năng trùng lặp.
- Xử lý dữ liệu lịch sử.

## 7.5. ERD

Tôi cần:

1. ERD tổng thể.
2. ERD theo từng phân hệ.
3. Sơ đồ phải có đầy đủ bảng và quan hệ.
4. Có PK, FK và Cardinality.
5. Không bỏ sót bảng trung gian.
6. Tên bảng phải thống nhất giữa các sơ đồ.
7. Các ERD phân hệ phải ghép được thành ERD tổng thể.

Sử dụng Mermaid ER Diagram hoặc công cụ phù hợp.

Nếu sơ đồ quá lớn, hãy chia nhỏ để dễ đọc nhưng vẫn duy trì tính nhất quán.

## 7.6. Database Normalization

Phân tích:

- 1NF.
- 2NF.
- 3NF.

Giải thích:
- Functional Dependency.
- Partial Dependency.
- Transitive Dependency.
- Data Redundancy.
- Insert Anomaly.
- Update Anomaly.
- Delete Anomaly.

Đưa ví dụ trực tiếp từ Database của dự án.

Nếu có phi chuẩn hóa, phải giải thích trade-off.

## 7.7. Data Dictionary

Với mỗi bảng, mô tả:

- Table Name.
- Table Description.
- Column Name.
- Data Type.
- Constraint.
- Description.
- PK/FK.
- Relationship.

## 7.8. Constraints và Index

Phân tích:

- Primary Key.
- Foreign Key.
- Unique.
- Not Null.
- Check.
- Default.
- Index.
- Composite Index.

Đề xuất Index dựa trên truy vấn thực tế.

## 7.9. Transaction và Data Integrity

Phân tích các nghiệp vụ cần Transaction.

Ưu tiên đặc biệt:

**Đăng ký học phần:**

- Kiểm tra điều kiện tiên quyết.
- Kiểm tra thời gian đăng ký.
- Kiểm tra sĩ số.
- Kiểm tra trùng lịch.
- Kiểm tra giới hạn tín chỉ.
- Chống đăng ký trùng.
- Xử lý hai sinh viên cùng đăng ký suất cuối cùng.
- Rollback nếu giao dịch thất bại.

Giải thích các vấn đề:
- ACID.
- Concurrency.
- Race Condition.
- Locking.
- Isolation Level.

Đề xuất cách giải quyết phù hợp với Database được lựa chọn.

## 7.10. SQL Implementation

Sau khi ERD được xác nhận:

- Viết CREATE TABLE.
- Thiết lập PK/FK.
- Constraints.
- Indexes.
- Các bảng trung gian.
- Migration.
- Sample Data để kiểm thử.

SQL phải khớp tuyệt đối với mô hình dữ liệu đã thiết kế.

Không được có tình trạng ERD một kiểu, Data Dictionary một kiểu và SQL một kiểu khác.

---

# 8. GIAI ĐOẠN 5: LỰA CHỌN TECH STACK

Tôi chưa chốt công nghệ. Bạn phải tư vấn có cơ sở.

## 8.1. So sánh Frontend

Xem xét:
- React.
- Vue.
- Angular.

Đánh giá:
- Độ khó.
- Khả năng xây dựng giao diện quản trị.
- Khả năng tái sử dụng component.
- Ecosystem.
- Khả năng phát triển và bảo trì.

## 8.2. So sánh Backend

Xem xét:
- Java Spring Boot.
- Node.js.
- ASP.NET Core.

Đánh giá:
- Khả năng xây dựng REST API.
- Xử lý Business Logic.
- Authentication và Authorization.
- ORM.
- Security.
- Testing.
- Khả năng mở rộng.
- Giá trị thể hiện trong Portfolio.

## 8.3. So sánh Database

Xem xét:
- PostgreSQL.
- MySQL.
- SQL Server.

Đánh giá:
- Quan hệ và ràng buộc dữ liệu.
- Transaction.
- Index.
- Query.
- Khả năng xử lý nghiệp vụ đào tạo.
- Công cụ hỗ trợ.
- Khả năng triển khai.

## 8.4. Tiêu chí lựa chọn

Ưu tiên:

1. Phù hợp với dự án quản lý đào tạo.
2. Phù hợp với một người phát triển.
3. Dễ học và có tài liệu tốt.
4. Dễ kiểm thử.
5. Dễ deploy.
6. Có giá trị khi trình bày trong Portfolio.
7. Có thể mở rộng về sau.

Hãy tạo bảng so sánh các phương án.

Sau đó đưa ra:
- Phương án khuyến nghị.
- Lý do lựa chọn.
- Trade-off.
- Công nghệ bổ trợ cần thiết.

Không chọn Microservices chỉ vì muốn dự án trông chuyên nghiệp.

## 8.5. Kiến trúc khuyến nghị

Hãy đánh giá Modular Monolith trước, vì đây là dự án cá nhân.

Nếu phù hợp, chia backend thành các module nghiệp vụ:

- Authentication.
- User Management.
- Student Management.
- Organization Management.
- Course Management.
- Enrollment.
- Grade Management.
- Reporting.

Không tạo module chỉ để chia nhỏ thư mục nếu không có trách nhiệm nghiệp vụ rõ ràng.

---

# 9. GIAI ĐOẠN 6: ARCHITECTURE & API DESIGN

## 9.1. System Architecture

Thiết kế:

- Presentation Layer.
- Controller/API Layer.
- Business Logic Layer.
- Data Access Layer.
- Database Layer.

Giải thích nhiệm vụ từng tầng và cách dữ liệu đi qua hệ thống.

## 9.2. API Design

Với mỗi API, ghi:

- Method.
- Endpoint.
- Purpose.
- Authorization.
- Request.
- Response.
- Validation.
- Error Code.

Ưu tiên RESTful API.

## 9.3. Security Design
# YÊU CẦU BẮT BUỘC: AUTHENTICATION & AUTHORIZATION

Đây là phân hệ nền tảng, phải được phân tích và thiết kế ngay từ đầu.

## 1. Authentication – Xác thực

Hệ thống phải hỗ trợ đăng nhập cho các nhóm người dùng:

- Admin.
- Cán bộ phòng đào tạo.
- Giảng viên.
- Sinh viên.
- Cố vấn học tập nếu có.

Các chức năng:
- Đăng nhập bằng tài khoản và mật khẩu.
- Đăng xuất.
- Đổi mật khẩu.
- Quên mật khẩu và đặt lại mật khẩu.
- Kiểm tra trạng thái tài khoản.
- Duy trì phiên đăng nhập theo cơ chế được lựa chọn.
- Bảo vệ các API yêu cầu đăng nhập.

## 2. Authorization – Phân quyền

Sử dụng Role-Based Access Control (RBAC).

Thiết kế các thành phần:
- User.
- Role.
- Permission.
- UserRole.
- RolePermission.

Phân tích mối quan hệ giữa các bảng và xây dựng ERD riêng cho phân hệ này.

Không chỉ phân quyền ở giao diện. Backend phải kiểm tra quyền truy cập trên từng API.

## 3. Quyền hạn từng nhóm người dùng

**Admin:**
- Quản lý tài khoản.
- Quản lý vai trò và quyền hạn.
- Quản lý dữ liệu hệ thống.
- Theo dõi nhật ký hoạt động.

**Phòng đào tạo:**
- Quản lý hồ sơ sinh viên.
- Quản lý học kỳ.
- Quản lý học phần.
- Quản lý lớp học phần.
- Quản lý đăng ký học phần.
- Quản lý kết quả đào tạo theo phạm vi được cấp quyền.

**Giảng viên:**
- Xem danh sách lớp học phần được phân công.
- Xem danh sách sinh viên trong lớp.
- Xem thời khóa biểu.
- Nhập và cập nhật điểm theo quyền hạn.
- Theo dõi kết quả học tập của lớp được phân công.

**Sinh viên:**
- Xem và cập nhật những thông tin cá nhân được phép.
- Xem chương trình đào tạo.
- Tra cứu học phần.
- Đăng ký và hủy đăng ký học phần theo quy định.
- Xem thời khóa biểu.
- Xem điểm và kết quả học tập.
- Tra cứu thông tin học phí nếu phân hệ này được triển khai.

## 4. Luồng đăng nhập

User nhập tài khoản và mật khẩu
→ Frontend gửi Request
→ Backend kiểm tra tài khoản
→ Kiểm tra mật khẩu
→ Kiểm tra trạng thái tài khoản
→ Xác thực thành công
→ Tạo Session/Token
→ Trả kết quả cho Frontend
→ Điều hướng đến giao diện phù hợp với vai trò.

## 5. Luồng kiểm tra quyền

User truy cập chức năng
→ Frontend gửi Request kèm thông tin xác thực
→ Backend xác thực người dùng
→ Kiểm tra Permission
→ Nếu có quyền: xử lý nghiệp vụ
→ Nếu không có quyền: trả về HTTP 403 Forbidden.

## 6. Yêu cầu thiết kế và kiểm thử

Phải có:
- Authentication Use Case Diagram.
- Authorization Use Case Diagram.
- Activity Diagram đăng nhập.
- Sequence Diagram đăng nhập.
- Sequence Diagram kiểm tra quyền.
- ERD phân hệ User/Role/Permission.
- API đăng nhập, đăng xuất, đổi mật khẩu.
- API quản lý tài khoản và phân quyền.
- Test Case cho từng vai trò.

Đặc biệt kiểm thử:
- Sinh viên không được truy cập API quản lý điểm.
- Giảng viên không được sửa điểm của lớp không được phân công.
- Sinh viên không được xem dữ liệu cá nhân của sinh viên khác nếu không được cấp quyền.
- Người dùng đã bị khóa không thể tiếp tục sử dụng hệ thống.
- Người chưa đăng nhập không thể truy cập API riêng tư.

Không chỉ ẩn nút trên giao diện mà phải bảo vệ dữ liệu ở Backend.
Phân tích:

- Authentication.
- Authorization.
- Role-Based Access Control.
- Password Hashing.
- JWT hoặc Session nếu phù hợp.
- Input Validation.
- SQL Injection.
- XSS.
- CSRF.
- Rate Limiting nếu cần.
- Audit Log.

Không lưu mật khẩu dạng plaintext.

## 9.4. Luồng xử lý thực tế

Mô tả từng bước:

User thao tác
→ Frontend
→ HTTP Request
→ Backend Controller
→ Authentication/Authorization
→ Validation
→ Business Service
→ Repository/DAO
→ Database
→ Response
→ Frontend hiển thị kết quả.

Lấy ví dụ trực tiếp từ nghiệp vụ đăng ký học phần.

---

# 10. GIAI ĐOẠN 7: PROJECT PLANNING

Dự án do một cá nhân thực hiện nên phải tối ưu thứ tự công việc và tránh phân tán.

## 10.1. Roadmap

Chia thành các phase:

1. Discovery & Requirement Analysis.
2. System Analysis & Design.
3. Database Design.
4. Environment Setup.
5. Backend Development.
6. Frontend Development.
7. Integration.
8. Testing.
9. Deployment.
10. Portfolio Documentation.

Mỗi phase cần:
- Mục tiêu.
- Task.
- Deliverables.
- Dependencies.
- Estimated Effort.
- Acceptance Criteria.

## 10.2. Product Backlog

Tổ chức theo cấu trúc:

Project
→ Epic
→ Feature
→ User Story
→ Task
→ Subtask.

Mỗi Task có:
- ID.
- Name.
- Description.
- Priority.
- Estimated Effort.
- Dependency.
- Acceptance Criteria.
- Status.

Phân biệt rõ công việc BA, Database, Backend, Frontend và Testing.

## 10.3. MVP và Future Scope

Đề xuất hai phạm vi riêng biệt.

**MVP:**

Chọn những phân hệ đủ để tạo thành một sản phẩm quản lý đào tạo có thể sử dụng và demo hoàn chỉnh.

Ưu tiên các nghiệp vụ thể hiện được:
- Quản lý sinh viên.
- Quản lý học phần.
- Quản lý học kỳ.
- Quản lý lớp học phần.
- Đăng ký học phần.
- Quản lý điểm.
- Tra cứu kết quả học tập.
- Phân quyền.

Đây là các chức năng gợi ý. Hãy kiểm tra tính cần thiết và mối phụ thuộc giữa chúng.

**Future Scope:**

Các chức năng như:
- Điểm danh.
- Học phí.
- Lịch thi.
- Phúc khảo.
- Thông báo nâng cao.
- Tích hợp hệ thống ngoài.

Chỉ mở rộng khi MVP đã hoàn thành tốt.

## 10.4. Timeline

Thời gian hiện chưa xác định.

Hãy:
- Ước lượng tổng effort theo giờ hoặc ngày công.
- Đề xuất timeline theo tuần.
- Đưa ra kịch bản tối giản, tiêu chuẩn và mở rộng.
- Nêu rõ giả định về số giờ làm việc mỗi tuần.
- Phân biệt thời gian phân tích, thiết kế, coding và kiểm thử.
- Dành thời gian cho việc sửa lỗi và hoàn thiện Portfolio.

Không đưa ra một deadline tùy tiện mà không có cơ sở.

## 10.5. Git Workflow

Đề xuất quy trình Git phù hợp cho cá nhân:

- Main.
- Develop nếu thực sự cần.
- Feature branches.
- Bugfix branches.
- Pull Request hoặc tự review.
- Commit Convention.

Đưa ra ví dụ lệnh Git và cách tổ chức repository.

---

# 11. GIAI ĐOẠN 8: TESTING & QUALITY ASSURANCE

Xây dựng Test Plan cho các chức năng.

Bao gồm:
- Unit Testing.
- Integration Testing.
- API Testing.
- System Testing.
- UI Testing.
- Security Testing.
- Performance Testing.
- Regression Testing.

Với các nghiệp vụ quan trọng, tạo Test Case gồm:

- Test Case ID.
- Scenario.
- Preconditions.
- Test Steps.
- Test Data.
- Expected Result.
- Priority.

Đặc biệt kiểm thử các trường hợp lỗi:

- Không đủ điều kiện đăng ký học phần.
- Trùng lịch.
- Lớp đã đủ sĩ số.
- Đăng ký trùng.
- Không đủ quyền.
- Dữ liệu không hợp lệ.
- Ghi điểm sai.
- Truy cập dữ liệu trái phép.
- Nhiều người thao tác đồng thời.

---

# 12. GIAI ĐOẠN 9: DEPLOYMENT & PORTFOLIO

Đây là sản phẩm tôi sẽ sử dụng để giới thiệu năng lực khi ứng tuyển.

Vì vậy, hãy đề xuất cách hoàn thiện dự án để người khác có thể truy cập, trải nghiệm và đánh giá.

Các đầu ra cần có:

1. Website được triển khai online nếu điều kiện hạ tầng cho phép.
2. Repository GitHub công khai.
3. README đầy đủ.
4. Giới thiệu bài toán.
5. Feature List.
6. Tech Stack.
7. System Architecture Diagram.
8. UML Diagrams.
9. ERD.
10. Database Documentation.
11. API Documentation.
12. Hướng dẫn cài đặt và chạy project.
13. Tài khoản demo với các vai trò khác nhau.
14. Dữ liệu mẫu giả lập.
15. Screenshot giao diện.
16. Test Results.
17. Demo Video nếu cần.

Không sử dụng dữ liệu sinh viên thật hoặc thông tin cá nhân nhạy cảm. Dữ liệu demo phải được tạo giả lập.

Đề xuất cấu trúc thư mục tài liệu trong repository để nhà tuyển dụng dễ tìm hiểu.

---

# 13. TIÊU CHUẨN CHẤT LƯỢNG ĐẦU RA

Tôi muốn dự án thể hiện được các năng lực sau:

**Business Analysis:**
- Hiểu bài toán thực tế.
- Biết phân tích nghiệp vụ.
- Xác định đúng phạm vi.
- Viết được yêu cầu chức năng.

**System Analysis & Design:**
- Sử dụng UML đúng.
- Xây dựng Use Case.
- Phân tích Activity và Sequence.
- Thiết kế Class Diagram.
- Hiểu kiến trúc hệ thống.

**Database:**
- Phân tích Entity.
- Xây dựng ERD.
- Thiết kế PK/FK.
- Chuẩn hóa dữ liệu.
- Thiết kế Index.
- Hiểu Transaction.
- Viết SQL.

**Development:**
- Xây dựng REST API.
- Authentication và Authorization.
- Xử lý Business Logic.
- Tổ chức code.
- Tích hợp Frontend và Backend.

**Software Quality:**
- Viết test.
- Xử lý lỗi.
- Bảo mật.
- Logging.
- Triển khai.

Không cần làm mọi thứ ở mức độ của một hệ thống production quy mô lớn. Nhưng những phần đã chọn phải được làm đúng, nhất quán và giải thích được.

---

# 14. QUY TẮC GIẢI THÍCH VÀ HƯỚNG DẪN

Tôi là Intern/Fresher nên cần bạn hướng dẫn theo cách một Senior đang training một thành viên mới trong công ty.

Khi đưa ra quyết định, hãy giải thích:

1. Vấn đề cần giải quyết là gì?
2. Tại sao cần giải quyết vấn đề này?
3. Cách làm thông thường trong thực tế.
4. Các phương án có thể sử dụng.
5. Tại sao chọn phương án được đề xuất?
6. Ưu và nhược điểm.
7. Trade-off.
8. Ví dụ thực tế trong chính dự án PTIT.

Khi mô tả quy trình, ưu tiên dùng sơ đồ:

User thao tác
→ System xử lý
→ Database
→ System trả kết quả.

Khi viết code ở các giai đoạn sau:
- Sử dụng code phổ biến trong dự án thực tế.
- Có comment tại những phần logic quan trọng.
- Giải thích vai trò của từng class/module.
- Không sử dụng code quá phức tạp không cần thiết.
- Không viết code trước khi thiết kế nghiệp vụ tương ứng được thống nhất.

---

# 15. CÁCH BẮT ĐẦU LÀM VIỆC

Bây giờ, hãy bắt đầu dự án với vai trò Senior BA và Project Architect.

**Trong câu trả lời đầu tiên, chỉ thực hiện những việc sau:**

1. Tóm tắt thông tin dự án bạn đã nhận được.
2. Phân tích những khó khăn chính của dự án.
3. Nêu những rủi ro khi một người tự thực hiện hệ thống quản lý đào tạo.
4. Đề xuất sơ bộ cách tổ chức dự án theo các giai đoạn.
5. Đề xuất các nhóm công nghệ cần lựa chọn, chưa được tự chốt.
6. Đưa ra danh sách câu hỏi khảo sát nghiệp vụ theo mức độ ưu tiên.

Hãy bắt đầu bằng những câu hỏi quan trọng nhất để xác định phạm vi MVP.

**Không vẽ UML, không tạo Database, không viết SQL và không lập trình ở câu trả lời đầu tiên.**

Sau khi tôi trả lời, tiếp tục làm rõ yêu cầu theo từng vòng.

Chỉ khi nghiệp vụ được xác nhận mới chuyển sang các giai đoạn phân tích, thiết kế và triển khai tiếp theo.

Mục tiêu cuối cùng là xây dựng một website quản lý đào tạo có nghiệp vụ hợp lý, Database được thiết kế tốt, kiến trúc rõ ràng, code có chất lượng và đủ hoàn thiện để trở thành một dự án Portfolio cá nhân có giá trị.