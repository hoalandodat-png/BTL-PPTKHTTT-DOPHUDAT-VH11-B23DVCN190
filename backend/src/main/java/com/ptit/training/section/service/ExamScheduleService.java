package com.ptit.training.section.service;

import com.ptit.training.common.exception.BusinessException;
import com.ptit.training.common.exception.ConflictException;
import com.ptit.training.common.exception.ResourceNotFoundException;
import com.ptit.training.common.security.UserPrincipal;
import com.ptit.training.enrollment.entity.Enrollment;
import com.ptit.training.enrollment.repository.EnrollmentRepository;
import com.ptit.training.section.dto.ExamDtos.*;
import com.ptit.training.section.entity.Classroom;
import com.ptit.training.section.entity.CourseSection;
import com.ptit.training.section.entity.ExamSchedule;
import com.ptit.training.section.repository.ClassroomRepository;
import com.ptit.training.section.repository.CourseSectionRepository;
import com.ptit.training.section.repository.ExamScheduleRepository;
import com.ptit.training.student.entity.Student;
import com.ptit.training.student.repository.StudentRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamScheduleService {

    private final ExamScheduleRepository examRepo;
    private final CourseSectionRepository sectionRepo;
    private final ClassroomRepository classroomRepo;
    private final StudentRepository studentRepo;
    private final EnrollmentRepository enrollmentRepo;

    public ExamScheduleService(
            ExamScheduleRepository examRepo,
            CourseSectionRepository sectionRepo,
            ClassroomRepository classroomRepo,
            StudentRepository studentRepo,
            EnrollmentRepository enrollmentRepo) {
        this.examRepo = examRepo;
        this.sectionRepo = sectionRepo;
        this.classroomRepo = classroomRepo;
        this.studentRepo = studentRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    @Transactional(readOnly = true)
    public List<ExamScheduleDto> listBySemester(Long semesterId) {
        return examRepo.findBySemesterIdWithDetails(semesterId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamScheduleDto> getMyExams(UserPrincipal principal, Long semesterId) {
        Student student = studentRepo.findByUserId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT_NOT_FOUND", "Không tìm thấy hồ sơ sinh viên"));
        List<Long> sectionIds = enrollmentRepo.findActiveByStudentAndSemesterWithDetails(student.getId(), semesterId)
                .stream()
                .map(e -> e.getSection().getId())
                .toList();
        if (sectionIds.isEmpty()) {
            return List.of();
        }
        return examRepo.findBySectionIdsWithDetails(sectionIds).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ExamScheduleDto createExam(CreateExamScheduleRequest req) {
        if (!req.endTime().isAfter(req.startTime())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_EXAM_TIME",
                    "Giờ kết thúc thi phải lớn hơn giờ bắt đầu");
        }
        CourseSection section = sectionRepo.findWithDetailsById(req.sectionId())
                .orElseThrow(() -> new ResourceNotFoundException("SECTION_NOT_FOUND", "Không tìm thấy lớp học phần"));
        Classroom room = classroomRepo.findById(req.classroomId())
                .orElseThrow(() -> new ResourceNotFoundException("CLASSROOM_NOT_FOUND", "Không tìm thấy phòng thi"));

        List<ExamSchedule> existingSameRoom = examRepo.findByClassroomAndDate(room.getId(), req.examDate());
        for (ExamSchedule other : existingSameRoom) {
            boolean overlap = req.startTime().isBefore(other.getEndTime())
                    && other.getStartTime().isBefore(req.endTime());
            if (overlap) {
                throw new ConflictException("EXAM_ROOM_CONFLICT",
                        "Phòng " + room.getCode() + " ngày " + req.examDate() + " đã có lịch thi lớp "
                                + other.getSection().getSectionCode());
            }
        }

        ExamSchedule es = new ExamSchedule();
        es.setSection(section);
        es.setClassroom(room);
        es.setExamDate(req.examDate());
        es.setStartTime(req.startTime());
        es.setEndTime(req.endTime());
        es.setExamFormat(req.examFormat().trim());
        es.setNote(req.note() != null && !req.note().isBlank() ? req.note().trim() : null);

        ExamSchedule saved = examRepo.save(es);
        return toDto(saved);
    }

    @Transactional
    public void deleteExam(Long id) {
        if (!examRepo.existsById(id)) {
            throw new ResourceNotFoundException("EXAM_NOT_FOUND", "Không tìm thấy lịch thi");
        }
        examRepo.deleteById(id);
    }

    private ExamScheduleDto toDto(ExamSchedule es) {
        CourseSection s = es.getSection();
        return new ExamScheduleDto(
                es.getId(),
                s.getId(),
                s.getSectionCode(),
                s.getCourse().getCode(),
                s.getCourse().getName(),
                s.getCourse().getCredits(),
                s.getSemester().getId(),
                s.getSemester().getCode(),
                es.getClassroom().getId(),
                es.getClassroom().getCode(),
                es.getClassroom().getBuilding(),
                es.getExamDate(),
                es.getStartTime(),
                es.getEndTime(),
                es.getExamFormat(),
                es.getNote()
        );
    }
}
