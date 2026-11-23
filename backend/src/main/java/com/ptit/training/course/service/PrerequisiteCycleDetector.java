package com.ptit.training.course.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Thuật toán kiểm tra đồ thị điều kiện tiên quyết có hướng không chu trình (DAG - BR-12).
 */
@Component
public class PrerequisiteCycleDetector {

    /**
     * Trả về true nếu việc gán danh sách candidatePrereqIds làm điều kiện cho courseId tạo ra chu trình.
     */
    public boolean createsCycle(Long courseId, List<Long> candidatePrereqIds, Map<Long, List<Long>> existingGraph) {
        Map<Long, List<Long>> graph = new HashMap<>(existingGraph);
        graph.put(courseId, candidatePrereqIds);

        Set<Long> visited = new HashSet<>();
        for (Long prereqId : candidatePrereqIds) {
            if (prereqId.equals(courseId)) {
                return true;
            }
            if (canReach(prereqId, courseId, graph, visited)) {
                return true;
            }
        }
        return false;
    }

    private boolean canReach(Long current, Long target, Map<Long, List<Long>> graph, Set<Long> visited) {
        if (current.equals(target)) {
            return true;
        }
        if (!visited.add(current)) {
            return false;
        }
        List<Long> neighbors = graph.getOrDefault(current, List.of());
        for (Long next : neighbors) {
            if (canReach(next, target, graph, visited)) {
                return true;
            }
        }
        return false;
    }
}
