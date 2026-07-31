package com.ojttracker.checklist;

import com.ojttracker.auth.Site;
import com.ojttracker.auth.SiteCode;
import com.ojttracker.auth.SiteRepository;
import com.ojttracker.auth.TokenPrincipal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 임시 1회성 유지보수 엔드포인트 (이슈 #58). 중계 체크리스트 구성을 다른 4개 지점에 완전히 덮어써서 재동기화한다.
 * 실행 후 이 컨트롤러는 제거될 예정이다.
 */
@RestController
@RequestMapping("/api/admin/checklist")
public class ChecklistResyncController {

    private final ChecklistItemRepository checklistItemRepository;
    private final SiteRepository siteRepository;

    public ChecklistResyncController(ChecklistItemRepository checklistItemRepository, SiteRepository siteRepository) {
        this.checklistItemRepository = checklistItemRepository;
        this.siteRepository = siteRepository;
    }

    @PostMapping("/resync-from-junggye")
    @Transactional
    public ResponseEntity<?> resyncFromJunggye(TokenPrincipal principal) {
        if (!principal.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "관리자만 실행할 수 있습니다."));
        }

        Long junggyeId = siteRepository
                .findByCode(SiteCode.JUNGGYE.name())
                .orElseThrow(() -> new IllegalStateException("중계 사이트가 없습니다."))
                .getId();
        List<ChecklistItem> sourceItems = checklistItemRepository.findAllSortedBySiteId(junggyeId);

        List<SiteCode> otherSites =
                Arrays.stream(SiteCode.values()).filter(code -> code != SiteCode.JUNGGYE).toList();

        Map<String, Integer> resyncedCounts = new LinkedHashMap<>();
        for (SiteCode code : otherSites) {
            Site site = siteRepository.findByCode(code.name()).orElseThrow();
            checklistItemRepository.deleteBySiteId(site.getId());
            List<ChecklistItem> clones = sourceItems.stream()
                    .map(item -> {
                        ChecklistItem clone = new ChecklistItem(item.getTitle(), item.getCategory(), site.getId());
                        clone.setSortOrder(item.getSortOrder());
                        return clone;
                    })
                    .toList();
            checklistItemRepository.saveAll(clones);
            resyncedCounts.put(code.name(), clones.size());
        }

        return ResponseEntity.ok(Map.of("sourceItemCount", sourceItems.size(), "resyncedSites", resyncedCounts));
    }
}
