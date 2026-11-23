package com.ptit.training.course.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "curriculum_courses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"curriculum_id", "course_id"})
)
public class CurriculumCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curriculum_id", nullable = false)
    private Curriculum curriculum;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "recommended_semester", nullable = false)
    private Integer recommendedSemester;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;

    public Long getId() { return id; }
    public Curriculum getCurriculum() { return curriculum; }
    public void setCurriculum(Curriculum curriculum) { this.curriculum = curriculum; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public Integer getRecommendedSemester() { return recommendedSemester; }
    public void setRecommendedSemester(Integer recommendedSemester) { this.recommendedSemester = recommendedSemester; }
    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean required) { isRequired = required; }
}
