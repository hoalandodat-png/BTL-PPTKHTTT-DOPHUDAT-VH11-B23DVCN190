package com.ptit.training.section.entity;

import com.ptit.training.course.entity.Course;
import com.ptit.training.organization.entity.Lecturer;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "course_sections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "semester_id", "group_number"})
)
public class CourseSection {

    public enum GradeBookStatus { DRAFT, PUBLISHED, LOCKED }
    public enum Status { OPEN, CLOSED, CANCELLED, COMPLETED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_code", nullable = false, unique = true, length = 40)
    private String sectionCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "group_number", nullable = false)
    private Integer groupNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id")
    private Lecturer lecturer;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity = 60;

    @Column(name = "current_enrolled", nullable = false)
    private Integer currentEnrolled = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_status", nullable = false, length = 20)
    private GradeBookStatus gradeStatus = GradeBookStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.OPEN;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayOfWeek ASC, startPeriod ASC")
    private List<SectionSchedule> schedules = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() { this.updatedAt = Instant.now(); }

    public boolean hasAvailableSeat() {
        return currentEnrolled < maxCapacity;
    }

    public void incrementEnrolled() {
        this.currentEnrolled = this.currentEnrolled + 1;
    }

    public void decrementEnrolled() {
        this.currentEnrolled = Math.max(0, this.currentEnrolled - 1);
    }

    public Long getId() { return id; }
    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }
    public Integer getGroupNumber() { return groupNumber; }
    public void setGroupNumber(Integer groupNumber) { this.groupNumber = groupNumber; }
    public Lecturer getLecturer() { return lecturer; }
    public void setLecturer(Lecturer lecturer) { this.lecturer = lecturer; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public Integer getCurrentEnrolled() { return currentEnrolled; }
    public void setCurrentEnrolled(Integer currentEnrolled) { this.currentEnrolled = currentEnrolled; }
    public GradeBookStatus getGradeStatus() { return gradeStatus; }
    public void setGradeStatus(GradeBookStatus gradeStatus) { this.gradeStatus = gradeStatus; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getLockedAt() { return lockedAt; }
    public void setLockedAt(Instant lockedAt) { this.lockedAt = lockedAt; }
    public List<SectionSchedule> getSchedules() { return schedules; }
    public void setSchedules(List<SectionSchedule> schedules) { this.schedules = schedules; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
