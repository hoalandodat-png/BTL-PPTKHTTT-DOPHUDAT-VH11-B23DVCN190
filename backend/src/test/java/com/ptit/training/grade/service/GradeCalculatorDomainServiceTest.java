package com.ptit.training.grade.service;

import com.ptit.training.course.entity.Course;
import com.ptit.training.course.entity.GradeComponent;
import com.ptit.training.enrollment.entity.Enrollment;
import com.ptit.training.grade.entity.GradingScale;
import com.ptit.training.section.entity.CourseSection;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class GradeCalculatorDomainServiceTest {

    private GradeCalculatorDomainService calculator;
    private List<GradeComponent> components;
    private List<GradingScale> scales;

    @BeforeEach
    void setUp() {
        calculator = new GradeCalculatorDomainService();

        GradeComponent cc = makeComp(1L, "ATTENDANCE", 10, false, "0.0");
        GradeComponent th = makeComp(2L, "PRACTICE", 20, false, "0.0");
        GradeComponent gk = makeComp(3L, "MIDTERM", 20, false, "0.0");
        GradeComponent ck = makeComp(4L, "FINAL", 50, true, "4.0");
        components = List.of(cc, th, gk, ck);

        scales = List.of(
                makeScale("8.5", "10.0", "A", "4.00", true),
                makeScale("8.0", "8.4", "B+", "3.50", true),
                makeScale("7.0", "7.9", "B", "3.00", true),
                makeScale("6.5", "6.9", "C+", "2.50", true),
                makeScale("5.5", "6.4", "C", "2.00", true),
                makeScale("5.0", "5.4", "D+", "1.50", true),
                makeScale("4.0", "4.9", "D", "1.00", true),
                makeScale("0.0", "3.9", "F", "0.00", false)
        );
    }

    @Test
    void tcGrd02_shouldComputeWeightedAverageAndMapToLetterA() {
        Map<Long, BigDecimal> scores = Map.of(
                1L, new BigDecimal("9.0"),
                2L, new BigDecimal("8.0"),
                3L, new BigDecimal("8.5"),
                4L, new BigDecimal("8.5")
        );

        var result = calculator.computeFinalGrade(components, scores, scales);

        assertThat(result).isNotNull();
        assertThat(result.finalScore10()).isEqualByComparingTo("8.5");
        assertThat(result.letterGrade()).isEqualTo("A");
        assertThat(result.gpaScore4()).isEqualByComparingTo("4.00");
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void tcGrd03_shouldFailWhenFinalExamBelowThresholdEvenIfWeightedAvgIsHigh() {
        // 10*0.1 + 10*0.2 + 10*0.2 + 3.5*0.5 = 6.75 -> 6.8, nhưng Cuối kỳ 3.5 < 4.0 (điểm liệt)
        Map<Long, BigDecimal> scores = Map.of(
                1L, new BigDecimal("10.0"),
                2L, new BigDecimal("10.0"),
                3L, new BigDecimal("10.0"),
                4L, new BigDecimal("3.5")
        );

        var result = calculator.computeFinalGrade(components, scores, scales);

        assertThat(result).isNotNull();
        assertThat(result.finalScore10()).isEqualByComparingTo("6.8");
        assertThat(result.letterGrade()).isEqualTo("F");
        assertThat(result.gpaScore4()).isEqualByComparingTo("0.00");
        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void tcGrd07_shouldTakeHighestScoreForRetakenCourseInCumulativeCpa() {
        Course c1 = new Course();
        ReflectionTestUtils.setField(c1, "id", 10L);
        c1.setCode("INT1154");
        c1.setCredits(3);

        Course c2 = new Course();
        ReflectionTestUtils.setField(c2, "id", 20L);
        c2.setCode("INT1306");
        c2.setCredits(3);

        CourseSection published = new CourseSection();
        published.setGradeStatus(CourseSection.GradeBookStatus.PUBLISHED);

        Enrollment failAttempt = makeEnrollment(c1, published, 1, "2.0", "F", "0.00", false);
        Enrollment retakeAttempt = makeEnrollment(c1, published, 2, "7.5", "B", "3.00", true);
        Enrollment secondCourse = makeEnrollment(c2, published, 1, "9.0", "A", "4.00", true);

        var cpa = calculator.computeCumulativeCpa(List.of(failAttempt, retakeAttempt, secondCourse), true);

        assertThat(cpa.cpa4()).isEqualByComparingTo("3.50");
        assertThat(cpa.accumulatedCredits()).isEqualTo(6);
    }

    private GradeComponent makeComp(Long id, String code, int weight, boolean isFinal, String threshold) {
        GradeComponent gc = new GradeComponent();
        ReflectionTestUtils.setField(gc, "id", id);
        gc.setCode(code);
        gc.setWeightPercent(weight);
        gc.setIsFinalExam(isFinal);
        gc.setMinThreshold(new BigDecimal(threshold));
        return gc;
    }

    private GradingScale makeScale(String min, String max, String letter, String gpa4, boolean pass) {
        GradingScale gs = new GradingScale();
        gs.setMinScore10(new BigDecimal(min));
        gs.setMaxScore10(new BigDecimal(max));
        gs.setLetterGrade(letter);
        gs.setGpaScore4(new BigDecimal(gpa4));
        gs.setIsPass(pass);
        return gs;
    }

    private Enrollment makeEnrollment(Course c, CourseSection sec, int attempt, String s10, String letter, String g4, boolean pass) {
        Enrollment e = new Enrollment();
        e.setCourse(c);
        e.setSection(sec);
        e.setAttemptNumber(attempt);
        e.setFinalScore10(new BigDecimal(s10));
        e.setLetterGrade(letter);
        e.setGpaScore4(new BigDecimal(g4));
        e.setIsPassed(pass);
        return e;
    }
}
