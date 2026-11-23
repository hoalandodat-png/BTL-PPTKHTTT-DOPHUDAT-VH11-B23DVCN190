package com.ptit.training.course.service;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrerequisiteCycleDetectorTest {

    private final PrerequisiteCycleDetector detector = new PrerequisiteCycleDetector();

    @Test
    void tcCrs02_shouldDetectDirectAndTransitiveCycles() {
        // Course 2 requires 1; Course 3 requires 2 (3 -> 2 -> 1)
        Map<Long, List<Long>> graph = Map.of(
                2L, List.of(1L),
                3L, List.of(2L)
        );

        // 1 -> 1 (self loop)
        assertThat(detector.createsCycle(1L, List.of(1L), graph)).isTrue();
        // 1 -> 2 creates 1 -> 2 -> 1
        assertThat(detector.createsCycle(1L, List.of(2L), graph)).isTrue();
        // 1 -> 3 creates 1 -> 3 -> 2 -> 1
        assertThat(detector.createsCycle(1L, List.of(3L), graph)).isTrue();
        // 4 -> 3 is valid DAG
        assertThat(detector.createsCycle(4L, List.of(3L), graph)).isFalse();
    }
}
