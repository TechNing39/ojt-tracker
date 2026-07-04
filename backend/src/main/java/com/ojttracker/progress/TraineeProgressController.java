package com.ojttracker.progress;

import com.ojttracker.checklist.ChecklistItem;
import com.ojttracker.checklist.ChecklistItemRepository;
import com.ojttracker.trainee.TraineeRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainees/{traineeId}/progress")
public class TraineeProgressController {

    private final TraineeRepository traineeRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final TraineeProgressRepository progressRepository;

    public TraineeProgressController(
            TraineeRepository traineeRepository,
            ChecklistItemRepository checklistItemRepository,
            TraineeProgressRepository progressRepository) {
        this.traineeRepository = traineeRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.progressRepository = progressRepository;
    }

    @GetMapping
    public ResponseEntity<?> list(@PathVariable Long traineeId) {
        if (!traineeRepository.existsById(traineeId)) {
            return ResponseEntity.notFound().build();
        }

        List<ChecklistItem> items = checklistItemRepository.findAll();
        Map<Long, TraineeProgress> progressByItemId = progressRepository.findByTraineeId(traineeId).stream()
                .collect(java.util.stream.Collectors.toMap(TraineeProgress::getChecklistItemId, p -> p));

        List<ProgressItemView> result = items.stream()
                .map(item -> {
                    TraineeProgress progress = progressByItemId.get(item.getId());
                    boolean completed = progress != null && progress.isCompleted();
                    var completedAt = progress != null ? progress.getCompletedAt() : null;
                    return new ProgressItemView(item.getId(), item.getTitle(), item.getCategory(), completed, completedAt);
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<?> toggle(@PathVariable Long traineeId, @PathVariable Long itemId) {
        if (!traineeRepository.existsById(traineeId)) {
            return ResponseEntity.notFound().build();
        }
        if (!checklistItemRepository.existsById(itemId)) {
            return ResponseEntity.notFound().build();
        }

        TraineeProgress progress = progressRepository
                .findByTraineeIdAndChecklistItemId(traineeId, itemId)
                .orElseGet(() -> new TraineeProgress(traineeId, itemId));
        progress.toggle();
        TraineeProgress saved = progressRepository.save(progress);

        return ResponseEntity.ok(saved);
    }
}
