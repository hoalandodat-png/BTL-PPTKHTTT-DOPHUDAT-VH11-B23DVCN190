package com.ptit.training.enrollment.entity;

import com.ptit.training.auth.entity.User;
import com.ptit.training.course.entity.Course;
import com.ptit.training.section.entity.CourseSection;
import com.ptit.training.section.entity.Semester;
import com.ptit.training.student.entity.Student;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "section_id"})
)
public class Enrollment {

    public enum Status { ENROLLED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ENROLLED;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber = 1;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Instant enrolledAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrolled_by", nullable = false)
    private User enrolledBy;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "final_score_10", precision = 4, scale = 1)
    private BigDecimal finalScore10;

    @Column(name = "letter_grade", length = 5)
    private String letterGrade;

    @Column(name = "gpa_score_4", precision = 3, scale = 2)
    private BigDecimal gpaScore4;

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() { this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public CourseSection getSection() { return section; }
    public void setSection(CourseSection section) { this.section = section; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }
    public Instant getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }
    public User getEnrolledBy() { return enrolledBy; }
    public void setEnrolledBy(User enrolledBy) { this.enrolledBy = enrolledBy; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public User getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(User cancelledBy) { this.cancelledBy = cancelledBy; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public BigDecimal getFinalScore10() { return finalScore10; }
    public void setFinalScore10(BigDecimal finalScore10) { this.finalScore10 = finalScore10; }
    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }
    public BigDecimal getGpaScore4() { return gpaScore4; }
    public void setGpaScore4(BigDecimal gpaScore4) { this.gpaScore4 = gpaScore4; }
    public Boolean getIsPassed() { return isPassed; }
    public void setIsPassed(Boolean passed) { isPassed = passed; }
    public Instant getUpdatedAt() { return updatedAt; }
}
