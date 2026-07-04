package com.ojttracker.checklist;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checklist-items")
public class ChecklistItemController {

    private final ChecklistItemRepository repository;

    public ChecklistItemController(ChecklistItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ChecklistItem> list() {
        return repository.findAll();
    }

    public record CreateChecklistItemRequest(String title) {
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateChecklistItemRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "title은 필수입니다."));
        }
        ChecklistItem saved = repository.save(new ChecklistItem(request.title()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
