package com.ojttracker.progress;

import java.time.Instant;

public record ProgressItemView(Long checklistItemId, String title, boolean completed, Instant completedAt) {
}
