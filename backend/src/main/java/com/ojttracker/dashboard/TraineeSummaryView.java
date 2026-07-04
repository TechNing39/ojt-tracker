package com.ojttracker.dashboard;

import com.ojttracker.checklist.Category;
import java.util.Map;

public record TraineeSummaryView(
        Long traineeId,
        String traineeName,
        int completedTotal,
        int totalItems,
        Map<Category, CategoryCount> byCategory) {
}
