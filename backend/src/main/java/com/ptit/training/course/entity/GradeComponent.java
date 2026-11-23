package com.ptit.training.course.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "grade_components",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "code"})
)
public class GradeComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "weight_percent", nullable = false)
    private Integer weightPercent;

    @Column(name = "is_final_exam", nullable = false)
    private Boolean isFinalExam = false;

    @Column(name = "min_threshold", nullable = false, precision = 4, scale = 1)
    private BigDecimal minThreshold = BigDecimal.ZERO;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 1;

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getWeightPercent() { return weightPercent; }
    public void setWeightPercent(Integer weightPercent) { this.weightPercent = weightPercent; }
    public Boolean getIsFinalExam() { return isFinalExam; }
    public void setIsFinalExam(Boolean finalExam) { isFinalExam = finalExam; }
    public BigDecimal getMinThreshold() { return minThreshold; }
    public void setMinThreshold(BigDecimal minThreshold) { this.minThreshold = minThreshold; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
