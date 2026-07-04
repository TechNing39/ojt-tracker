package com.ojttracker.progress;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class TraineeProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long traineeId;

    private Long checklistItemId;

    private boolean completed;

    private Instant completedAt;

    protected TraineeProgress() {
    }

    public TraineeProgress(Long traineeId, Long checklistItemId) {
        this.traineeId = traineeId;
        this.checklistItemId = checklistItemId;
        this.completed = false;
        this.completedAt = null;
    }

    public void toggle() {
        this.completed = !this.completed;
        this.completedAt = this.completed ? Instant.now() : null;
    }

    public Long getId() {
        return id;
    }

    public Long getTraineeId() {
        return traineeId;
    }

    public Long getChecklistItemId() {
        return checklistItemId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
