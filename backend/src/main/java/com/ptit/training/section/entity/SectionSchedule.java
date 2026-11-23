package com.ptit.training.section.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "section_schedules")
public class SectionSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "start_period", nullable = false)
    private Integer startPeriod;

    @Column(name = "end_period", nullable = false)
    private Integer endPeriod;

    @Column(name = "start_week", nullable = false)
    private Integer startWeek = 1;

    @Column(name = "end_week", nullable = false)
    private Integer endWeek = 15;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    /**
     * Kiểm tra trùng lịch theo BR-15: cùng Thứ, giao khoảng tuần và giao khoảng tiết.
     */
    public boolean overlapsWith(SectionSchedule other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        boolean weekOverlaps = this.startWeek <= other.endWeek && other.startWeek <= this.endWeek;
        if (!weekOverlaps) {
            return false;
        }
        return this.startPeriod <= other.endPeriod && other.startPeriod <= this.endPeriod;
    }

    public Long getId() { return id; }
    public CourseSection getSection() { return section; }
    public void setSection(CourseSection section) { this.section = section; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Integer getStartPeriod() { return startPeriod; }
    public void setStartPeriod(Integer startPeriod) { this.startPeriod = startPeriod; }
    public Integer getEndPeriod() { return endPeriod; }
    public void setEndPeriod(Integer endPeriod) { this.endPeriod = endPeriod; }
    public Integer getStartWeek() { return startWeek; }
    public void setStartWeek(Integer startWeek) { this.startWeek = startWeek; }
    public Integer getEndWeek() { return endWeek; }
    public void setEndWeek(Integer endWeek) { this.endWeek = endWeek; }
    public Classroom getClassroom() { return classroom; }
    public void setClassroom(Classroom classroom) { this.classroom = classroom; }
}
