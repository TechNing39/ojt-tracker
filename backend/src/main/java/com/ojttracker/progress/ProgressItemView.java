package com.ojttracker.progress;

import com.ojttracker.checklist.Category;
import java.time.Instant;

public record ProgressItemView(
        Long checklistItemId, String title, Category category, boolean completed, Instant completedAt) {
}
