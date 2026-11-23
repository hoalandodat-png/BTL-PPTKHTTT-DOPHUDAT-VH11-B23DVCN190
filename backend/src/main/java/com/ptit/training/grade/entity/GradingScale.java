package com.ptit.training.grade.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "grading_scales")
public class GradingScale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_score_10", nullable = false, precision = 4, scale = 1)
    private BigDecimal minScore10;

    @Column(name = "max_score_10", nullable = false, precision = 4, scale = 1)
    private BigDecimal maxScore10;

    @Column(name = "letter_grade", nullable = false, unique = true, length = 5)
    private String letterGrade;

    @Column(name = "gpa_score_4", nullable = false, precision = 3, scale = 2)
    private BigDecimal gpaScore4;

    @Column(name = "is_pass", nullable = false)
    private Boolean isPass;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public Long getId() { return id; }
    public BigDecimal getMinScore10() { return minScore10; }
    public void setMinScore10(BigDecimal minScore10) { this.minScore10 = minScore10; }
    public BigDecimal getMaxScore10() { return maxScore10; }
    public void setMaxScore10(BigDecimal maxScore10) { this.maxScore10 = maxScore10; }
    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }
    public BigDecimal getGpaScore4() { return gpaScore4; }
    public void setGpaScore4(BigDecimal gpaScore4) { this.gpaScore4 = gpaScore4; }
    public Boolean getIsPass() { return isPass; }
    public void setIsPass(Boolean pass) { isPass = pass; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
