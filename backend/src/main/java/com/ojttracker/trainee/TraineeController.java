package com.ojttracker.trainee;

import com.ojttracker.progress.TraineeProgressRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/trainees")
public class TraineeController {

    private final TraineeRepository repository;
    private final TraineeProgressRepository progressRepository;

    public TraineeController(TraineeRepository repository, TraineeProgressRepository progressRepository) {
        this.repository = repository;
        this.progressRepository = progressRepository;
    }

    @GetMapping
    public List<Trainee> list() {
        return repository.findAll();
    }

    public record CreateTraineeRequest(String name) {
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateTraineeRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "name은 필수입니다."));
        }
        Trainee saved = repository.save(new Trainee(request.name()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public record UpdateNoteRequest(String note) {
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Long id, @RequestBody UpdateNoteRequest request) {
        return repository.findById(id)
                .map(trainee -> {
                    trainee.setNote(request.note());
                    return ResponseEntity.ok(repository.save(trainee));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        progressRepository.deleteByTraineeId(id);
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
