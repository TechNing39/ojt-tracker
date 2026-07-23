package com.ojttracker.checklist;

import com.ojttracker.auth.SiteAccessGuard;
import com.ojttracker.auth.TokenPrincipal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/checklist-items")
public class ChecklistItemController {

    private final ChecklistItemRepository repository;
    private final SiteAccessGuard siteAccessGuard;

    public ChecklistItemController(ChecklistItemRepository repository, SiteAccessGuard siteAccessGuard) {
        this.repository = repository;
        this.siteAccessGuard = siteAccessGuard;
    }

    @GetMapping
    public List<ChecklistItem> list(TokenPrincipal principal, @RequestParam(required = false) Long siteId) {
        return repository.findAllSortedBySiteId(siteAccessGuard.resolveSiteId(principal, siteId));
    }

    public record CreateChecklistItemRequest(String title, Category category) {
    }

    @PostMapping
    public ResponseEntity<?> create(
            TokenPrincipal principal,
            @RequestParam(required = false) Long siteId,
            @RequestBody CreateChecklistItemRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "title은 필수입니다."));
        }
        if (request.category() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "category는 필수입니다."));
        }
        Long resolvedSiteId = siteAccessGuard.resolveSiteId(principal, siteId);
        ChecklistItem item = new ChecklistItem(request.title(), request.category(), resolvedSiteId);
        item.setSortOrder((int) repository.countByCategoryAndSiteId(request.category(), resolvedSiteId));
        ChecklistItem saved = repository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public record ReorderChecklistItemsRequest(Category category, List<Long> orderedIds) {
    }

    @PatchMapping("/reorder")
    public ResponseEntity<?> reorder(
            TokenPrincipal principal,
            @RequestParam(required = false) Long siteId,
            @RequestBody ReorderChecklistItemsRequest request) {
        if (request.category() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "category는 필수입니다."));
        }
        if (request.orderedIds() == null || request.orderedIds().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "orderedIds는 필수입니다."));
        }

        Long resolvedSiteId = siteAccessGuard.resolveSiteId(principal, siteId);
        List<ChecklistItem> categoryItems = repository.findByCategoryAndSiteId(request.category(), resolvedSiteId);
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

        return ResponseEntity.ok(repository.findAllSortedBySiteId(resolvedSiteId));
    }

    public record UpdateChecklistItemRequest(String title) {
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(
            TokenPrincipal principal, @PathVariable Long id, @RequestBody UpdateChecklistItemRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "title은 필수입니다."));
        }
        return findOwned(principal, id)
                .map(item -> {
                    item.setTitle(request.title());
                    return ResponseEntity.ok(repository.save(item));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(TokenPrincipal principal, @PathVariable Long id) {
        if (findOwned(principal, id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Optional<ChecklistItem> findOwned(TokenPrincipal principal, Long id) {
        return principal.isAdmin() ? repository.findById(id) : repository.findByIdAndSiteId(id, principal.siteId());
    }
}
