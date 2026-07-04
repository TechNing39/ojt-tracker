package com.ojttracker.progress;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraineeProgressRepository extends JpaRepository<TraineeProgress, Long> {

    List<TraineeProgress> findByTraineeId(Long traineeId);

    Optional<TraineeProgress> findByTraineeIdAndChecklistItemId(Long traineeId, Long checklistItemId);
}
