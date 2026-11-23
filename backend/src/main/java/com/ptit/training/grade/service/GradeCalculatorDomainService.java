package com.ptit.training.grade.service;

import com.ptit.training.course.entity.GradeComponent;
import com.ptit.training.enrollment.entity.Enrollment;
import com.ptit.training.grade.entity.GradingScale;
import com.ptit.training.section.entity.CourseSection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Domain Service thuần túy thực hiện công thức tính điểm tổng kết, xét điểm liệt, GPA học kỳ & CPA tích lũy (BR-28..31).
 */
@Service
public class GradeCalculatorDomainService {

    public record GradeCalculationResult(
            BigDecimal finalScore10,
            String letterGrade,
            BigDecimal gpaScore4,
            boolean isPassed
    ) {}

    public record CpaSummary(
            BigDecimal cpa4,
            int accumulatedCredits
    ) {}

    /**
     * Tính điểm tổng kết hệ 10, kiểm tra điểm liệt cuối kỳ và quy đổi sang điểm chữ / hệ 4.
     */
    public GradeCalculationResult computeFinalGrade(
            List<GradeComponent> components,
            Map<Long, BigDecimal> scoreByComponentId,
            List<GradingScale> scales) {

        BigDecimal weightedSum = BigDecimal.ZERO;
        boolean failedThreshold = false;

        for (GradeComponent comp : components) {
            BigDecimal score = scoreByComponentId.get(comp.getId());
            if (score == null) {
                return null;
            }
            BigDecimal normalized = score.setScale(1, RoundingMode.HALF_UP);
            if (Boolean.TRUE.equals(comp.getIsFinalExam())
                    && comp.getMinThreshold() != null
                    && normalized.compareTo(comp.getMinThreshold()) < 0) {
                failedThreshold = true;
            }
            BigDecimal weight = BigDecimal.valueOf(comp.getWeightPercent());
            weightedSum = weightedSum.add(normalized.multiply(weight));
        }

        BigDecimal finalScore10 = weightedSum.divide(BigDecimal.valueOf(100), 1, RoundingMode.HALF_UP);

        if (failedThreshold) {
            return new GradeCalculationResult(finalScore10, "F", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), false);
        }

        for (GradingScale scale : scales) {
            if (finalScore10.compareTo(scale.getMinScore10()) >= 0
                    && finalScore10.compareTo(scale.getMaxScore10()) <= 0) {
                return new GradeCalculationResult(
                        finalScore10,
                        scale.getLetterGrade(),
                        scale.getGpaScore4().setScale(2, RoundingMode.HALF_UP),
                        Boolean.TRUE.equals(scale.getIsPass()));
            }
        }

        return new GradeCalculationResult(finalScore10, "F", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), false);
    }

    /**
     * Tính GPA cho danh sách các môn đã công bố trong một học kỳ.
     */
    public BigDecimal computeSemesterGpa4(List<Enrollment> publishedInSemester) {
        BigDecimal totalWeightedGpa = BigDecimal.ZERO;
        int totalCredits = 0;

        for (Enrollment e : publishedInSemester) {
            if (e.getGpaScore4() == null) continue;
            int credits = e.getCourse().getCredits();
            totalWeightedGpa = totalWeightedGpa.add(e.getGpaScore4().multiply(BigDecimal.valueOf(credits)));
            totalCredits += credits;
        }

        if (totalCredits == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return totalWeightedGpa.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);
    }

    /**
     * Tính CPA tích lũy toàn khóa và tổng tín chỉ tích lũy, xử lý học lại / cải thiện theo policy (BR-31).
     */
    public CpaSummary computeCumulativeCpa(List<Enrollment> allEnrolled, boolean useHighestScorePolicy) {
        Map<Long, Enrollment> representativeByCourse = new HashMap<>();

        for (Enrollment e : allEnrolled) {
            if (e.getSection().getGradeStatus() == CourseSection.GradeBookStatus.DRAFT
                    || e.getGpaScore4() == null) {
                continue;
            }
            Long courseId = e.getCourse().getId();
            Enrollment currentBest = representativeByCourse.get(courseId);
            if (currentBest == null) {
                representativeByCourse.put(courseId, e);
            } else if (useHighestScorePolicy) {
                int cmp = e.getGpaScore4().compareTo(currentBest.getGpaScore4());
                if (cmp > 0 || (cmp == 0 && e.getFinalScore10().compareTo(currentBest.getFinalScore10()) > 0)) {
                    representativeByCourse.put(courseId, e);
                }
            } else {
                if (e.getAttemptNumber() >= currentBest.getAttemptNumber()) {
                    representativeByCourse.put(courseId, e);
                }
            }
        }

        BigDecimal totalWeightedGpa = BigDecimal.ZERO;
        int totalGpaCredits = 0;
        int accumulatedPassedCredits = 0;

        List<Enrollment> reps = representativeByCourse.values().stream()
                .sorted(Comparator.comparing(e -> e.getCourse().getCode()))
                .toList();

        for (Enrollment e : reps) {
            int credits = e.getCourse().getCredits();
            totalWeightedGpa = totalWeightedGpa.add(e.getGpaScore4().multiply(BigDecimal.valueOf(credits)));
            totalGpaCredits += credits;
            if (Boolean.TRUE.equals(e.getIsPassed())) {
                accumulatedPassedCredits += credits;
            }
        }

        BigDecimal cpa4 = totalGpaCredits == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : totalWeightedGpa.divide(BigDecimal.valueOf(totalGpaCredits), 2, RoundingMode.HALF_UP);

        return new CpaSummary(cpa4, accumulatedPassedCredits);
    }
}
