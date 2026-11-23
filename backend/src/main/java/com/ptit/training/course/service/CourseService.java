package com.ptit.training.course.service;

import com.ptit.training.common.PageResponse;
import com.ptit.training.common.exception.BusinessException;
import com.ptit.training.common.exception.ConflictException;
import com.ptit.training.common.exception.ResourceNotFoundException;
import com.ptit.training.course.dto.CourseDtos.*;
import com.ptit.training.course.entity.*;
import com.ptit.training.course.repository.*;
import com.ptit.training.organization.entity.Department;
import com.ptit.training.organization.entity.Faculty;
import com.ptit.training.organization.entity.Major;
import com.ptit.training.organization.repository.DepartmentRepository;
import com.ptit.training.organization.repository.MajorRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quản lý Học phần, Thành phần điểm (tổng 100%), Điều kiện tiên quyết (DAG) và Chương trình đào tạo.
 */
@Service
public class CourseService {

    private final CourseRepository courseRepo;
    private final CoursePrerequisiteRepository prereqRepo;
    private final CurriculumRepository curriculumRepo;
    private final DepartmentRepository departmentRepo;
    private final MajorRepository majorRepo;
    private final PrerequisiteCycleDetector cycleDetector;

    public CourseService(
            CourseRepository courseRepo,
            CoursePrerequisiteRepository prereqRepo,
            CurriculumRepository curriculumRepo,
            DepartmentRepository departmentRepo,
            MajorRepository majorRepo,
            PrerequisiteCycleDetector cycleDetector) {
        this.courseRepo = courseRepo;
        this.prereqRepo = prereqRepo;
        this.curriculumRepo = curriculumRepo;
        this.departmentRepo = departmentRepo;
        this.majorRepo = majorRepo;
        this.cycleDetector = cycleDetector;
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseDto> searchCourses(String keyword, Long departmentId, Faculty.Status status, Pageable pageable) {
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Course> page = courseRepo.search(cleanKeyword, departmentId, status, pageable);
        return PageResponse.from(page.map(this::toCourseDto));
    }

    @Transactional(readOnly = true)
    public CourseDetailDto getCourseDetail(Long id) {
        Course c = courseRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("COURSE_NOT_FOUND", "Không tìm thấy học phần"));
        List<PrerequisiteDto> prereqs = prereqRepo.findByCourseId(id).stream()
                .map(this::toPrerequisiteDto)
                .toList();
        return toCourseDetailDto(c, prereqs);
    }

    @Transactional
    public CourseDetailDto createCourse(CreateCourseRequest req) {
        String code = req.code().trim().toUpperCase();
        if (courseRepo.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("COURSE_CODE_EXISTS", "Mã học phần đã tồn tại: " + code);
        }
        validateGradeComponents(req.gradeComponents());

        Department dept = departmentRepo.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("DEPARTMENT_NOT_FOUND", "Không tìm thấy bộ môn"));

        Course course = new Course();
        course.setCode(code);
        course.setName(req.name().trim());
        course.setCredits(req.credits());
        course.setTheoryHours(req.theoryHours());
        course.setPracticeHours(req.practiceHours());
        course.setDepartment(dept);

        applyGradeComponents(course, req.gradeComponents());
        Course saved = courseRepo.save(course);
        return getCourseDetail(saved.getId());
    }

    @Transactional
    public CourseDetailDto updateCourse(Long id, UpdateCourseRequest req) {
        Course course = courseRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("COURSE_NOT_FOUND", "Không tìm thấy học phần"));
        validateGradeComponents(req.gradeComponents());

        Department dept = departmentRepo.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("DEPARTMENT_NOT_FOUND", "Không tìm thấy bộ môn"));

        course.setName(req.name().trim());
        course.setCredits(req.credits());
        course.setTheoryHours(req.theoryHours());
        course.setPracticeHours(req.practiceHours());
        course.setDepartment(dept);
        course.setStatus(req.status());

        course.getGradeComponents().clear();
        courseRepo.flush();
        applyGradeComponents(course, req.gradeComponents());
        courseRepo.save(course);

        return getCourseDetail(id);
    }

    @Transactional
    public List<PrerequisiteDto> updatePrerequisites(Long courseId, UpdatePrerequisitesRequest req) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("COURSE_NOT_FOUND", "Không tìm thấy học phần"));

        List<PrerequisiteItemRequest> items = req.prerequisites() != null ? req.prerequisites() : List.of();
        Set<Long> seen = new HashSet<>();
        List<Long> candidateIds = new ArrayList<>();
        for (PrerequisiteItemRequest item : items) {
            if (item.prerequisiteCourseId().equals(courseId)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "PREREQUISITE_SELF_REFERENCE",
                        "Học phần không thể tự làm điều kiện của chính nó");
            }
            if (!seen.add(item.prerequisiteCourseId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "PREREQUISITE_DUPLICATE",
                        "Danh sách học phần điều kiện bị trùng lặp");
            }
            candidateIds.add(item.prerequisiteCourseId());
        }

        // Xây dựng đồ thị hiện tại để kiểm tra chống chu trình (BR-12)
        Map<Long, List<Long>> existingGraph = new HashMap<>();
        for (CoursePrerequisite cp : prereqRepo.findAll()) {
            existingGraph.computeIfAbsent(cp.getCourse().getId(), k -> new ArrayList<>())
                    .add(cp.getPrerequisiteCourse().getId());
        }
        if (cycleDetector.createsCycle(courseId, candidateIds, existingGraph)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PREREQUISITE_CYCLE_DETECTED",
                    "Thiết lập điều kiện tiên quyết tạo ra vòng lặp phụ thuộc (chu trình)");
        }

        prereqRepo.deleteByCourseId(courseId);
        prereqRepo.flush();

        List<CoursePrerequisite> savedList = new ArrayList<>();
        for (PrerequisiteItemRequest item : items) {
            Course reqCourse = courseRepo.findById(item.prerequisiteCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("PREREQUISITE_COURSE_NOT_FOUND",
                            "Không tìm thấy học phần điều kiện ID=" + item.prerequisiteCourseId()));
            CoursePrerequisite cp = new CoursePrerequisite();
            cp.setCourse(course);
            cp.setPrerequisiteCourse(reqCourse);
            cp.setConditionType(item.conditionType());
            savedList.add(prereqRepo.save(cp));
        }

        return savedList.stream().map(this::toPrerequisiteDto).toList();
    }

    // --- CURRICULUM ---
    @Transactional(readOnly = true)
    public List<CurriculumDto> listCurriculums(Long majorId, Integer cohortYear) {
        return curriculumRepo.search(majorId, cohortYear).stream()
                .map(c -> new CurriculumDto(c.getId(), c.getCode(), c.getName(),
                        c.getMajor().getId(), c.getMajor().getCode(), c.getMajor().getName(),
                        c.getCohortYear(), c.getTotalCredits(), c.getStatus().name()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CurriculumDetailDto getCurriculumDetail(Long id) {
        Curriculum c = curriculumRepo.findWithCoursesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CURRICULUM_NOT_FOUND", "Không tìm thấy chương trình đào tạo"));
        return toCurriculumDetailDto(c);
    }

    @Transactional
    public CurriculumDetailDto createCurriculum(CreateCurriculumRequest req) {
        String code = req.code().trim().toUpperCase();
        if (curriculumRepo.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("CURRICULUM_CODE_EXISTS", "Mã chương trình đào tạo đã tồn tại: " + code);
        }
        if (curriculumRepo.existsByMajorIdAndCohortYear(req.majorId(), req.cohortYear())) {
            throw new ConflictException("CURRICULUM_ALREADY_EXISTS",
                    "Đã tồn tại chương trình đào tạo cho ngành và khóa này");
        }
        Major major = majorRepo.findById(req.majorId())
                .orElseThrow(() -> new ResourceNotFoundException("MAJOR_NOT_FOUND", "Không tìm thấy ngành đào tạo"));

        Curriculum curr = new Curriculum();
        curr.setCode(code);
        curr.setName(req.name().trim());
        curr.setMajor(major);
        curr.setCohortYear(req.cohortYear());
        curr.setTotalCredits(req.totalCredits());

        Set<Long> seenCourseIds = new HashSet<>();
        for (CurriculumCourseItemDto item : req.courses()) {
            if (!seenCourseIds.add(item.courseId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "CURRICULUM_DUPLICATE_COURSE",
                        "Học phần ID=" + item.courseId() + " bị lặp trong chương trình đào tạo");
            }
            Course course = courseRepo.findById(item.courseId())
                    .orElseThrow(() -> new ResourceNotFoundException("COURSE_NOT_FOUND", "Không tìm thấy học phần ID=" + item.courseId()));
            CurriculumCourse cc = new CurriculumCourse();
            cc.setCurriculum(curr);
            cc.setCourse(course);
            cc.setRecommendedSemester(item.recommendedSemester());
            cc.setIsRequired(item.isRequired());
            curr.getCurriculumCourses().add(cc);
        }

        Curriculum saved = curriculumRepo.save(curr);
        return getCurriculumDetail(saved.getId());
    }

    private void validateGradeComponents(List<GradeComponentDto> components) {
        int sum = components.stream().mapToInt(GradeComponentDto::weightPercent).sum();
        if (sum != 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "GRADE_WEIGHT_SUM_NOT_100",
                    "Tổng trọng số các thành phần điểm phải bằng đúng 100% (hiện tại: " + sum + "%)");
        }
        Set<String> codes = new HashSet<>();
        for (GradeComponentDto gc : components) {
            if (!codes.add(gc.code().trim().toUpperCase())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "GRADE_COMPONENT_DUPLICATE_CODE",
                        "Trùng mã thành phần điểm: " + gc.code());
            }
        }
    }

    private void applyGradeComponents(Course course, List<GradeComponentDto> dtos) {
        for (GradeComponentDto dto : dtos) {
            GradeComponent gc = new GradeComponent();
            gc.setCourse(course);
            gc.setCode(dto.code().trim().toUpperCase());
            gc.setName(dto.name().trim());
            gc.setWeightPercent(dto.weightPercent());
            gc.setIsFinalExam(dto.isFinalExam());
            gc.setMinThreshold(dto.minThreshold());
            gc.setDisplayOrder(dto.displayOrder());
            course.getGradeComponents().add(gc);
        }
    }

    private CourseDto toCourseDto(Course c) {
        return new CourseDto(
                c.getId(), c.getCode(), c.getName(), c.getCredits(), c.getTheoryHours(), c.getPracticeHours(),
                c.getDepartment().getId(), c.getDepartment().getName(),
                c.getDepartment().getFaculty().getName(), c.getStatus().name()
        );
    }

    private CourseDetailDto toCourseDetailDto(Course c, List<PrerequisiteDto> prereqs) {
        List<GradeComponentDto> gcs = c.getGradeComponents().stream()
                .map(gc -> new GradeComponentDto(
                        gc.getId(), gc.getCode(), gc.getName(), gc.getWeightPercent(),
                        gc.getIsFinalExam(), gc.getMinThreshold(), gc.getDisplayOrder()))
                .toList();
        return new CourseDetailDto(
                c.getId(), c.getCode(), c.getName(), c.getCredits(), c.getTheoryHours(), c.getPracticeHours(),
                c.getDepartment().getId(), c.getDepartment().getName(), c.getStatus().name(), gcs, prereqs
        );
    }

    private PrerequisiteDto toPrerequisiteDto(CoursePrerequisite cp) {
        Course pre = cp.getPrerequisiteCourse();
        return new PrerequisiteDto(
                cp.getId(), pre.getId(), pre.getCode(), pre.getName(), pre.getCredits(), cp.getConditionType().name()
        );
    }

    private CurriculumDetailDto toCurriculumDetailDto(Curriculum c) {
        List<CurriculumCourseItemDto> items = c.getCurriculumCourses().stream()
                .map(cc -> new CurriculumCourseItemDto(
                        cc.getId(), cc.getCourse().getId(), cc.getCourse().getCode(),
                        cc.getCourse().getName(), cc.getCourse().getCredits(),
                        cc.getRecommendedSemester(), cc.getIsRequired()))
                .toList();
        return new CurriculumDetailDto(
                c.getId(), c.getCode(), c.getName(), c.getMajor().getId(), c.getMajor().getCode(),
                c.getMajor().getName(), c.getCohortYear(), c.getTotalCredits(), c.getStatus().name(), items
        );
    }
}
