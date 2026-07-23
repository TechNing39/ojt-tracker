package com.ojttracker.trainee;

import com.ojttracker.auth.SiteAccessGuard;
import com.ojttracker.auth.TokenPrincipal;
import com.ojttracker.progress.TraineeProgressRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainees")
public class TraineeController {

    private final TraineeRepository repository;
    private final TraineeProgressRepository progressRepository;
    private final SiteAccessGuard siteAccessGuard;

    public TraineeController(
            TraineeRepository repository,
            TraineeProgressRepository progressRepository,
            SiteAccessGuard siteAccessGuard) {
        this.repository = repository;
        this.progressRepository = progressRepository;
        this.siteAccessGuard = siteAccessGuard;
    }

    @GetMapping
    public List<Trainee> list(TokenPrincipal principal, @RequestParam(required = false) Long siteId) {
        return repository.findBySiteId(siteAccessGuard.resolveSiteId(principal, siteId));
    }

    public record CreateTraineeRequest(String name) {
    }

    @PostMapping
    public ResponseEntity<?> create(
            TokenPrincipal principal,
            @RequestParam(required = false) Long siteId,
            @RequestBody CreateTraineeRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "name은 필수입니다."));
        }
        Long resolvedSiteId = siteAccessGuard.resolveSiteId(principal, siteId);
        Trainee saved = repository.save(new Trainee(request.name(), resolvedSiteId));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public record UpdateNoteRequest(String note) {
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateNote(
            TokenPrincipal principal, @PathVariable Long id, @RequestBody UpdateNoteRequest request) {
        return findOwned(principal, id)
                .map(trainee -> {
                    trainee.setNote(request.note());
                    return ResponseEntity.ok(repository.save(trainee));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(TokenPrincipal principal, @PathVariable Long id) {
        Optional<Trainee> trainee = findOwned(principal, id);
        if (trainee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        progressRepository.deleteByTraineeId(id);
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Optional<Trainee> findOwned(TokenPrincipal principal, Long id) {
        return principal.isAdmin() ? repository.findById(id) : repository.findByIdAndSiteId(id, principal.siteId());
    }
}
