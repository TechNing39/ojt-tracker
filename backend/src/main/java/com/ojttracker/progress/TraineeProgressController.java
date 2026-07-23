package com.ojttracker.progress;

import com.ojttracker.auth.TokenPrincipal;
import com.ojttracker.checklist.ChecklistItem;
import com.ojttracker.checklist.ChecklistItemRepository;
import com.ojttracker.trainee.Trainee;
import com.ojttracker.trainee.TraineeRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public ResponseEntity<?> list(TokenPrincipal principal, @PathVariable Long traineeId) {
        Optional<Trainee> trainee = findOwnedTrainee(principal, traineeId);
        if (trainee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ChecklistItem> items = checklistItemRepository.findAllSortedBySiteId(trainee.get().getSiteId());
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
    public ResponseEntity<?> toggle(TokenPrincipal principal, @PathVariable Long traineeId, @PathVariable Long itemId) {
        Optional<Trainee> trainee = findOwnedTrainee(principal, traineeId);
        if (trainee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (checklistItemRepository.findByIdAndSiteId(itemId, trainee.get().getSiteId()).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TraineeProgress progress = progressRepository
                .findByTraineeIdAndChecklistItemId(traineeId, itemId)
                .orElseGet(() -> new TraineeProgress(traineeId, itemId));
        progress.toggle();
        TraineeProgress saved = progressRepository.save(progress);

        return ResponseEntity.ok(saved);
    }

    private Optional<Trainee> findOwnedTrainee(TokenPrincipal principal, Long traineeId) {
        return principal.isAdmin()
                ? traineeRepository.findById(traineeId)
                : traineeRepository.findByIdAndSiteId(traineeId, principal.siteId());
    }
}
