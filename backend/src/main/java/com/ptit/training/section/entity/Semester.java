package com.ptit.training.section.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "semesters")
public class Semester {

    public enum TermType { REGULAR, SUMMER }
    public enum Status { UPCOMING, ONGOING, CLOSED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "term_type", nullable = false, length = 20)
    private TermType termType = TermType.REGULAR;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "min_credits", nullable = false)
    private Integer minCredits = 12;

    @Column(name = "max_credits", nullable = false)
    private Integer maxCredits = 24;

    @Column(name = "registration_start_at", nullable = false)
    private Instant registrationStartAt;

    @Column(name = "registration_end_at", nullable = false)
    private Instant registrationEndAt;

    @Column(name = "is_registration_open", nullable = false)
    private Boolean isRegistrationOpen = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.UPCOMING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() { this.updatedAt = Instant.now(); }

    public boolean isRegistrationActive(Instant now) {
        return Boolean.TRUE.equals(isRegistrationOpen)
                && !now.isBefore(registrationStartAt)
                && !now.isAfter(registrationEndAt);
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AcademicYear getAcademicYear() { return academicYear; }
    public void setAcademicYear(AcademicYear academicYear) { this.academicYear = academicYear; }
    public TermType getTermType() { return termType; }
    public void setTermType(TermType termType) { this.termType = termType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getMinCredits() { return minCredits; }
    public void setMinCredits(Integer minCredits) { this.minCredits = minCredits; }
    public Integer getMaxCredits() { return maxCredits; }
    public void setMaxCredits(Integer maxCredits) { this.maxCredits = maxCredits; }
    public Instant getRegistrationStartAt() { return registrationStartAt; }
    public void setRegistrationStartAt(Instant registrationStartAt) { this.registrationStartAt = registrationStartAt; }
    public Instant getRegistrationEndAt() { return registrationEndAt; }
    public void setRegistrationEndAt(Instant registrationEndAt) { this.registrationEndAt = registrationEndAt; }
    public Boolean getIsRegistrationOpen() { return isRegistrationOpen; }
    public void setIsRegistrationOpen(Boolean registrationOpen) { isRegistrationOpen = registrationOpen; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
