package com.ojttracker.trainee;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainees")
public class TraineeController {

    private final TraineeRepository repository;

    public TraineeController(TraineeRepository repository) {
        this.repository = repository;
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
}
