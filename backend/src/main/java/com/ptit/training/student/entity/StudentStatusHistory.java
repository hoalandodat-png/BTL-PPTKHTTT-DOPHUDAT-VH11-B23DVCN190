package com.ptit.training.student.entity;

import com.ptit.training.auth.entity.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "student_status_history")
public class StudentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private Student.AcademicStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private Student.AcademicStatus newStatus;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt = Instant.now();

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Student.AcademicStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(Student.AcademicStatus oldStatus) { this.oldStatus = oldStatus; }
    public Student.AcademicStatus getNewStatus() { return newStatus; }
    public void setNewStatus(Student.AcademicStatus newStatus) { this.newStatus = newStatus; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }
    public Instant getChangedAt() { return changedAt; }
}
