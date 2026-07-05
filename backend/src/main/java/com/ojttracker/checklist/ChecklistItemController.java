package com.ojttracker.checklist;

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

@RestController
@RequestMapping("/api/checklist-items")
public class ChecklistItemController {

    private final ChecklistItemRepository repository;

    public ChecklistItemController(ChecklistItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ChecklistItem> list() {
        return repository.findAllSorted();
    }

    public record CreateChecklistItemRequest(String title, Category category) {
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateChecklistItemRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "title은 필수입니다."));
        }
        if (request.category() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "category는 필수입니다."));
        }
        ChecklistItem item = new ChecklistItem(request.title(), request.category());
        item.setSortOrder((int) repository.countByCategory(request.category()));
        ChecklistItem saved = repository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public record ReorderChecklistItemsRequest(Category category, List<Long> orderedIds) {
    }

    @PatchMapping("/reorder")
    public ResponseEntity<?> reorder(@RequestBody ReorderChecklistItemsRequest request) {
        if (request.category() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "category는 필수입니다."));
        }
        if (request.orderedIds() == null || request.orderedIds().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "orderedIds는 필수입니다."));
        }

        List<ChecklistItem> categoryItems = repository.findByCategory(request.category());
        Map<Long, ChecklistItem> itemsById =
                categoryItems.stream().collect(java.util.stream.Collectors.toMap(ChecklistItem::getId, i -> i));

        if (itemsById.size() != request.orderedIds().size() || !itemsById.keySet().containsAll(request.orderedIds())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "orderedIds가 해당 카테고리의 항목 구성과 일치하지 않습니다."));
        }

        List<Long> orderedIds = request.orderedIds();
        for (int i = 0; i < orderedIds.size(); i++) {
            itemsById.get(orderedIds.get(i)).setSortOrder(i);
        }
        repository.saveAll(itemsById.values());

        return ResponseEntity.ok(repository.findAllSorted());
    }

    public record UpdateChecklistItemRequest(String title) {
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdateChecklistItemRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "title은 필수입니다."));
        }
        return repository.findById(id)
                .map(item -> {
                    item.setTitle(request.title());
                    return ResponseEntity.ok(repository.save(item));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
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
