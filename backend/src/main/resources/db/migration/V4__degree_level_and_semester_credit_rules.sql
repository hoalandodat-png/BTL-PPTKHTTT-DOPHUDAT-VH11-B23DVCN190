-- =============================================================================
-- V4: BỔ SUNG HỆ ĐÀO TẠO (CỬ NHÂN / KỸ SƯ) VÀ QUY ĐỊNH TÍN CHỈ THEO LOẠI HỌC KỲ
--   - Học kỳ chính - hệ cử nhân (REGULAR + BACHELOR): Tối thiểu 15, Tối đa 25
--   - Học kỳ chính - hệ kỹ sư (REGULAR + ENGINEER):  Tối thiểu 16, Tối đa 25
--   - Học kỳ phụ (hè) (SUMMER):                        Không quy định chung (0), Tối đa 12
-- =============================================================================

ALTER TABLE majors
    ADD COLUMN IF NOT EXISTS degree_level VARCHAR(20) NOT NULL DEFAULT 'BACHELOR';

ALTER TABLE majors
    DROP CONSTRAINT IF EXISTS ck_majors_degree_level;

ALTER TABLE majors
    ADD CONSTRAINT ck_majors_degree_level CHECK (degree_level IN ('BACHELOR', 'ENGINEER'));

-- Cập nhật ngành An toàn thông tin (DCAT) sang hệ kỹ sư, CNTT (DCCN) giữ hệ cử nhân
UPDATE majors SET degree_level = 'ENGINEER' WHERE code = 'DCAT';
UPDATE majors SET degree_level = 'BACHELOR' WHERE code = 'DCCN';

-- Cập nhật chuẩn tín chỉ mặc định của các học kỳ chính hiện có: 15 - 25 TC
UPDATE semesters
SET min_credits = 15, max_credits = 25
WHERE term_type = 'REGULAR';

-- Bổ sung 1 học kỳ phụ (hè) mẫu 20263 để đối chiếu / kiểm thử quy định tối đa 12 tín chỉ
INSERT INTO semesters (
    id, code, name, academic_year_id, term_type, start_date, end_date,
    min_credits, max_credits, registration_start_at, registration_end_at, is_registration_open, status
)
SELECT
    3, '20263', 'Học kỳ phụ (Hè) Năm học 2026-2027',
    (SELECT id FROM academic_years WHERE code = '2026-2027' LIMIT 1),
    'SUMMER', '2027-06-01', '2027-07-25',
    0, 12, '2027-05-15T08:00:00+07:00', '2027-07-15T23:59:59+07:00', FALSE, 'UPCOMING'
WHERE EXISTS (SELECT 1 FROM academic_years WHERE code = '2026-2027')
  AND NOT EXISTS (SELECT 1 FROM semesters WHERE code = '20263');

SELECT setval(pg_get_serial_sequence('semesters', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM semesters), 10));
