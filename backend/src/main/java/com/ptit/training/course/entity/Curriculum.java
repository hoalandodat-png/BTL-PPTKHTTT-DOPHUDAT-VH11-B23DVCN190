package com.ptit.training.course.entity;

import com.ptit.training.organization.entity.Faculty;
import com.ptit.training.organization.entity.Major;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "curriculums",
        uniqueConstraints = @UniqueConstraint(columnNames = {"major_id", "cohort_year"})
)
public class Curriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(name = "cohort_year", nullable = false)
    private Integer cohortYear;

    @Column(name = "total_credits", nullable = false)
    private Integer totalCredits = 150;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Faculty.Status status = Faculty.Status.ACTIVE;

    @OneToMany(mappedBy = "curriculum", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("recommendedSemester ASC")
    private List<CurriculumCourse> curriculumCourses = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() { this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Major getMajor() { return major; }
    public void setMajor(Major major) { this.major = major; }
    public Integer getCohortYear() { return cohortYear; }
    public void setCohortYear(Integer cohortYear) { this.cohortYear = cohortYear; }
    public Integer getTotalCredits() { return totalCredits; }
    public void setTotalCredits(Integer totalCredits) { this.totalCredits = totalCredits; }
    public Faculty.Status getStatus() { return status; }
    public void setStatus(Faculty.Status status) { this.status = status; }
    public List<CurriculumCourse> getCurriculumCourses() { return curriculumCourses; }
    public void setCurriculumCourses(List<CurriculumCourse> curriculumCourses) { this.curriculumCourses = curriculumCourses; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
