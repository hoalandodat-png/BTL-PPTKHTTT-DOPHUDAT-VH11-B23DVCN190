package com.ptit.training.organization.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "majors")
public class Major {

    public enum DegreeLevel { BACHELOR, ENGINEER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(name = "total_credits_required", nullable = false)
    private Integer totalCreditsRequired = 150;

    @Enumerated(EnumType.STRING)
    @Column(name = "degree_level", nullable = false, length = 20)
    private DegreeLevel degreeLevel = DegreeLevel.BACHELOR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Faculty.Status status = Faculty.Status.ACTIVE;

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
    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }
    public Integer getTotalCreditsRequired() { return totalCreditsRequired; }
    public void setTotalCreditsRequired(Integer totalCreditsRequired) { this.totalCreditsRequired = totalCreditsRequired; }
    public DegreeLevel getDegreeLevel() { return degreeLevel; }
    public void setDegreeLevel(DegreeLevel degreeLevel) { this.degreeLevel = degreeLevel; }
    public Faculty.Status getStatus() { return status; }
    public void setStatus(Faculty.Status status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
