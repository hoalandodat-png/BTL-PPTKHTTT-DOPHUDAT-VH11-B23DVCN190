-- =============================================================================
-- V2: DỮ LIỆU MẪU GIẢ LẬP PTIT (DEMO SEED DATA)
-- Mật khẩu demo cho toàn bộ tài khoản: "Ptit@12345"
-- =============================================================================

INSERT INTO roles (id, code, name, description) VALUES
(1, 'ADMIN',            'Quản trị hệ thống',      'Quản lý tài khoản, phân quyền RBAC, cấu hình và Audit Log'),
(2, 'ACADEMIC_AFFAIRS', 'Phòng Quản lý Đào tạo',  'Quản lý học vụ, sinh viên, học phần, mở lớp, khóa điểm'),
(3, 'LECTURER',         'Giảng viên',             'Xem lớp phân công, nhập và công bố điểm lớp học phần'),
(4, 'STUDENT',          'Sinh viên',              'Đăng ký học phần, xem thời khóa biểu, kết quả học tập');

INSERT INTO permissions (id, code, name, module) VALUES
(1,  'USER_READ',            'Xem tài khoản',                       'AUTH'),
(2,  'USER_WRITE',           'Tạo và cập nhật tài khoản',           'AUTH'),
(3,  'USER_LOCK',            'Khóa và mở khóa tài khoản',           'AUTH'),
(4,  'RBAC_MANAGE',          'Quản lý vai trò và quyền hạn',        'AUTH'),
(5,  'ORG_READ',             'Xem danh mục Khoa/Bộ môn/Ngành/Lớp',  'ORG'),
(6,  'ORG_WRITE',            'Quản lý Khoa/Bộ môn/Ngành/Lớp/GV',    'ORG'),
(7,  'STUDENT_READ',         'Xem hồ sơ sinh viên',                 'STUDENT'),
(8,  'STUDENT_WRITE',        'Tạo, sửa và import hồ sơ sinh viên',  'STUDENT'),
(9,  'STUDENT_SELF_UPDATE',  'Sinh viên tự sửa thông tin liên lạc', 'STUDENT'),
(10, 'COURSE_READ',          'Xem học phần và chương trình đào tạo','COURSE'),
(11, 'COURSE_WRITE',         'Quản lý học phần, tiên quyết, CTĐT',  'COURSE'),
(12, 'SEMESTER_WRITE',       'Quản lý học kỳ và đợt đăng ký',       'SECTION'),
(13, 'SECTION_READ',         'Tra cứu lớp học phần và lịch học',    'SECTION'),
(14, 'SECTION_WRITE',        'Mở lớp học phần và xếp lịch học',     'SECTION'),
(15, 'ENROLLMENT_SELF',      'Sinh viên tự đăng ký và hủy học phần','ENROLLMENT'),
(16, 'ENROLLMENT_OVERRIDE',  'Phòng đào tạo đăng ký/hủy hộ',        'ENROLLMENT'),
(17, 'GRADE_WRITE',          'Giảng viên nhập điểm lớp phụ trách',  'GRADE'),
(18, 'GRADE_PUBLISH',        'Công bố điểm lớp học phần',           'GRADE'),
(19, 'GRADE_LOCK',           'Khóa sổ điểm và sửa điểm sau khóa',   'GRADE'),
(20, 'RESULT_READ_SELF',     'Sinh viên xem kết quả học tập',       'GRADE'),
(21, 'REPORT_READ',          'Xem báo cáo thống kê đào tạo',        'REPORT'),
(22, 'AUDIT_READ',           'Tra cứu nhật ký kiểm toán',           'SYSTEM');

INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 21), (1, 22),
(2, 1), (2, 5), (2, 6), (2, 7), (2, 8), (2, 10), (2, 11), (2, 12), (2, 13), (2, 14), (2, 16), (2, 18), (2, 19), (2, 21),
(3, 5), (3, 7), (3, 10), (3, 13), (3, 17), (3, 18), (3, 21),
(4, 5), (4, 9), (4, 10), (4, 13), (4, 15), (4, 20);

INSERT INTO grading_scales (id, min_score_10, max_score_10, letter_grade, gpa_score_4, is_pass, display_order) VALUES
(1, 8.5, 10.0, 'A',  4.00, TRUE,  1),
(2, 8.0,  8.4, 'B+', 3.50, TRUE,  2),
(3, 7.0,  7.9, 'B',  3.00, TRUE,  3),
(4, 6.5,  6.9, 'C+', 2.50, TRUE,  4),
(5, 5.5,  6.4, 'C',  2.00, TRUE,  5),
(6, 5.0,  5.4, 'D+', 1.50, TRUE,  6),
(7, 4.0,  4.9, 'D',  1.00, TRUE,  7),
(8, 0.0,  3.9, 'F',  0.00, FALSE, 8);

INSERT INTO system_configs (config_key, config_value, value_type, description) VALUES
('RETAKE_GPA_POLICY',            'HIGHEST_SCORE', 'STRING',  'Cách tính điểm CPA khi học lại: HIGHEST_SCORE (lấy điểm cao nhất) hoặc LATEST_SCORE'),
('DEFAULT_FINAL_EXAM_THRESHOLD', '4.0',           'DECIMAL', 'Ngưỡng điểm liệt thi cuối kỳ mặc định');

-- Mật khẩu demo "Ptit@12345" (hash sẽ được DataInitializer xác minh/cập nhật nếu cần)
INSERT INTO users (id, username, email, password_hash, status) VALUES
(1, 'admin',      'admin@ptit-demo.edu.vn',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE'),
(2, 'pdt01',      'daotao01@ptit-demo.edu.vn',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE'),
(3, 'GV001',      'gv.huy@ptit-demo.edu.vn',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE'),
(4, 'GV002',      'gv.lan@ptit-demo.edu.vn',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE'),
(5, 'B22DCCN001', 'b22dccn001@stu.ptit-demo.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE'),
(6, 'B22DCCN002', 'b22dccn002@stu.ptit-demo.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE');

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), (2, 2), (3, 3), (4, 3), (5, 4), (6, 4);

INSERT INTO faculties (id, code, name, description) VALUES
(1, 'CNTT1', 'Khoa Công nghệ thông tin 1', 'Đào tạo ngành Công nghệ thông tin và Khoa học máy tính'),
(2, 'ATTT',  'Khoa An toàn thông tin',     'Đào tạo chuyên sâu về An toàn thông tin');

INSERT INTO departments (id, code, name, faculty_id) VALUES
(1, 'CNPM', 'Bộ môn Công nghệ phần mềm', 1),
(2, 'HTTT', 'Bộ môn Hệ thống thông tin', 1),
(3, 'ATBM', 'Bộ môn An toàn mạng',       2);

INSERT INTO majors (id, code, name, faculty_id, total_credits_required) VALUES
(1, 'DCCN', 'Công nghệ thông tin', 1, 150),
(2, 'DCAT', 'An toàn thông tin',   2, 150);

INSERT INTO academic_classes (id, code, name, major_id, cohort_year) VALUES
(1, 'D22CQCN01-B', 'Lớp CNTT 01 - Khóa D22', 1, 2022),
(2, 'D22CQAT01-B', 'Lớp ATTT 01 - Khóa D22', 2, 2022);

INSERT INTO lecturers (id, user_id, lecturer_code, full_name, degree, academic_rank, department_id, phone) VALUES
(1, 3, 'GV001', 'TS. Nguyễn Quang Huy', 'TS',  'PGS', 1, '0901000111'),
(2, 4, 'GV002', 'ThS. Trần Thị Mai Lan', 'ThS', NULL,  2, '0901000222');

INSERT INTO students (id, user_id, student_code, full_name, date_of_birth, gender, national_id, personal_email, phone, address, academic_class_id, major_id, cohort_year, academic_status) VALUES
(1, 5, 'B22DCCN001', 'Phạm Minh Tuấn', '2004-05-12', 'MALE',   '001204000001', 'tuan.pm@gmail.com', '0912000001', 'Hà Đông, Hà Nội', 1, 1, 2022, 'ACTIVE'),
(2, 6, 'B22DCCN002', 'Lê Thu Hà',      '2004-09-20', 'FEMALE', '001204000002', 'ha.lt@gmail.com',   '0912000002', 'Cầu Giấy, Hà Nội', 1, 1, 2022, 'ACTIVE');

INSERT INTO student_status_history (id, student_id, old_status, new_status, reason, changed_by) VALUES
(1, 1, NULL, 'ACTIVE', 'Nhập học khóa D22', 2),
(2, 2, NULL, 'ACTIVE', 'Nhập học khóa D22', 2);

INSERT INTO courses (id, code, name, credits, theory_hours, practice_hours, department_id) VALUES
(1, 'INT1154', 'Tin học cơ sở 2 (Lập trình C)',      3, 30, 15, 1),
(2, 'INT1306', 'Cấu trúc dữ liệu và Giải thuật',    3, 30, 15, 1),
(3, 'INT1313', 'Cơ sở dữ liệu',                      3, 30, 15, 2),
(4, 'INT1332', 'Lập trình hướng đối tượng (Java)',  3, 30, 15, 1);

INSERT INTO course_prerequisites (id, course_id, prerequisite_course_id, condition_type) VALUES
(1, 2, 1, 'PREREQUISITE'),
(2, 4, 1, 'PREREQUISITE');

INSERT INTO grade_components (course_id, code, name, weight_percent, is_final_exam, min_threshold, display_order) VALUES
(1, 'ATTENDANCE', 'Chuyên cần',           10, FALSE, 0.0, 1),
(1, 'PRACTICE',   'Kiểm tra / Thực hành', 20, FALSE, 0.0, 2),
(1, 'MIDTERM',    'Giữa kỳ',              20, FALSE, 0.0, 3),
(1, 'FINAL',      'Cuối kỳ',              50, TRUE,  4.0, 4),
(2, 'ATTENDANCE', 'Chuyên cần',           10, FALSE, 0.0, 1),
(2, 'PRACTICE',   'Kiểm tra / Thực hành', 20, FALSE, 0.0, 2),
(2, 'MIDTERM',    'Giữa kỳ',              20, FALSE, 0.0, 3),
(2, 'FINAL',      'Cuối kỳ',              50, TRUE,  4.0, 4),
(3, 'ATTENDANCE', 'Chuyên cần',           10, FALSE, 0.0, 1),
(3, 'PRACTICE',   'Kiểm tra / Thực hành', 20, FALSE, 0.0, 2),
(3, 'MIDTERM',    'Giữa kỳ',              20, FALSE, 0.0, 3),
(3, 'FINAL',      'Cuối kỳ',              50, TRUE,  4.0, 4),
(4, 'ATTENDANCE', 'Chuyên cần',           10, FALSE, 0.0, 1),
(4, 'PRACTICE',   'Kiểm tra / Thực hành', 20, FALSE, 0.0, 2),
(4, 'MIDTERM',    'Giữa kỳ',              20, FALSE, 0.0, 3),
(4, 'FINAL',      'Cuối kỳ',              50, TRUE,  4.0, 4);

INSERT INTO curriculums (id, code, name, major_id, cohort_year, total_credits) VALUES
(1, 'CURR-DCCN-2022', 'Chương trình đào tạo CNTT Khóa 2022', 1, 2022, 150);

INSERT INTO curriculum_courses (curriculum_id, course_id, recommended_semester, is_required) VALUES
(1, 1, 2, TRUE),
(1, 2, 3, TRUE),
(1, 3, 4, TRUE),
(1, 4, 4, TRUE);

INSERT INTO academic_years (id, code, name, start_year, end_year, status) VALUES
(1, '2025-2026', 'Năm học 2025 - 2026', 2025, 2026, 'ARCHIVED'),
(2, '2026-2027', 'Năm học 2026 - 2027', 2026, 2027, 'ACTIVE');

INSERT INTO semesters (id, code, name, academic_year_id, term_type, start_date, end_date, min_credits, max_credits, registration_start_at, registration_end_at, is_registration_open, status) VALUES
(1, '20252', 'Học kỳ 2 Năm học 2025-2026', 1, 'REGULAR', '2026-01-15', '2026-05-30', 12, 24, '2026-01-01T08:00:00+07:00', '2026-01-14T23:59:59+07:00', FALSE, 'CLOSED'),
(2, '20261', 'Học kỳ 1 Năm học 2026-2027', 2, 'REGULAR', '2026-09-01', '2027-01-15', 12, 24, '2026-08-15T08:00:00+07:00', '2027-01-15T23:59:59+07:00', TRUE,  'ONGOING');

INSERT INTO classrooms (id, code, building, capacity, room_type) VALUES
(1, '401-A2',     'A2', 60, 'THEORY'),
(2, '402-A2',     'A2', 60, 'THEORY'),
(3, 'LAB-301-A3', 'A3', 40, 'LAB');

INSERT INTO course_sections (id, section_code, course_id, semester_id, group_number, lecturer_id, max_capacity, current_enrolled, grade_status, status) VALUES
(1, 'INT1154-20252-01', 1, 1, 1, 1, 60, 1, 'LOCKED', 'COMPLETED'),
(2, 'INT1306-20261-01', 2, 2, 1, 1, 50, 1, 'DRAFT',  'OPEN'),
(3, 'INT1313-20261-01', 3, 2, 1, 2, 50, 0, 'DRAFT',  'OPEN'),
(4, 'INT1332-20261-01', 4, 2, 1, 1, 50, 0, 'DRAFT',  'OPEN');

INSERT INTO section_schedules (section_id, day_of_week, start_period, end_period, start_week, end_week, classroom_id) VALUES
(1, 2, 1, 3, 1, 15, 1),
(2, 2, 1, 3, 1, 15, 1),
(3, 4, 7, 9, 1, 15, 2),
(4, 6, 1, 3, 1, 15, 3);

INSERT INTO enrollments (id, student_id, section_id, course_id, semester_id, status, attempt_number, enrolled_by, final_score_10, letter_grade, gpa_score_4, is_passed) VALUES
(1, 1, 1, 1, 1, 'ENROLLED', 1, 5, 8.5, 'A', 4.00, TRUE),
(2, 1, 2, 2, 2, 'ENROLLED', 1, 5, NULL, NULL, NULL, NULL);

SELECT setval(pg_get_serial_sequence('users', 'id'), 10);
SELECT setval(pg_get_serial_sequence('roles', 'id'), 10);
SELECT setval(pg_get_serial_sequence('permissions', 'id'), 30);
SELECT setval(pg_get_serial_sequence('faculties', 'id'), 10);
SELECT setval(pg_get_serial_sequence('departments', 'id'), 10);
SELECT setval(pg_get_serial_sequence('majors', 'id'), 10);
SELECT setval(pg_get_serial_sequence('academic_classes', 'id'), 10);
SELECT setval(pg_get_serial_sequence('lecturers', 'id'), 10);
SELECT setval(pg_get_serial_sequence('students', 'id'), 10);
SELECT setval(pg_get_serial_sequence('student_status_history', 'id'), 10);
SELECT setval(pg_get_serial_sequence('courses', 'id'), 10);
SELECT setval(pg_get_serial_sequence('course_prerequisites', 'id'), 10);
SELECT setval(pg_get_serial_sequence('grade_components', 'id'), 30);
SELECT setval(pg_get_serial_sequence('curriculums', 'id'), 10);
SELECT setval(pg_get_serial_sequence('curriculum_courses', 'id'), 20);
SELECT setval(pg_get_serial_sequence('academic_years', 'id'), 10);
SELECT setval(pg_get_serial_sequence('semesters', 'id'), 10);
SELECT setval(pg_get_serial_sequence('classrooms', 'id'), 10);
SELECT setval(pg_get_serial_sequence('course_sections', 'id'), 10);
SELECT setval(pg_get_serial_sequence('section_schedules', 'id'), 10);
SELECT setval(pg_get_serial_sequence('enrollments', 'id'), 10);
SELECT setval(pg_get_serial_sequence('grading_scales', 'id'), 10);
