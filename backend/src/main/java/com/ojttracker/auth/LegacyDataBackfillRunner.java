package com.ojttracker.auth;

import com.ojttracker.checklist.ChecklistItem;
import com.ojttracker.checklist.ChecklistItemRepository;
import com.ojttracker.trainee.TraineeRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * siteId가 없는 기존 데이터(멀티사이트 도입 이전)를 중계 지점으로 backfill하고,
 * 그 순간(=최초 마이그레이션 시점)에만 중계의 체크리스트 구성을 나머지 4개 지점의 기본값으로 복사한다.
 * 이후에는 지점별로 체크리스트가 독립적으로 관리되며, 이 클래스는 영구 no-op이 된다.
 */
@Component
@Order(2)
public class LegacyDataBackfillRunner implements ApplicationRunner {

    private final SiteRepository siteRepository;
    private final TraineeRepository traineeRepository;
    private final ChecklistItemRepository checklistItemRepository;

    public LegacyDataBackfillRunner(
            SiteRepository siteRepository,
            TraineeRepository traineeRepository,
            ChecklistItemRepository checklistItemRepository) {
        this.siteRepository = siteRepository;
        this.traineeRepository = traineeRepository;
        this.checklistItemRepository = checklistItemRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long junggyeId = siteRepository
                .findByCode(SiteCode.JUNGGYE.name())
                .orElseThrow(() -> new IllegalStateException("중계 사이트가 시딩되지 않았습니다."))
                .getId();

        traineeRepository.backfillSiteId(junggyeId);
        int backfilledChecklistCount = checklistItemRepository.backfillSiteId(junggyeId);

        if (backfilledChecklistCount > 0) {
            cloneChecklistToOtherSites(junggyeId);
        }
    }

    private void cloneChecklistToOtherSites(Long sourceSiteId) {
        List<ChecklistItem> sourceItems = checklistItemRepository.findAllSortedBySiteId(sourceSiteId);

        List<SiteCode> otherSites =
                Arrays.stream(SiteCode.values()).filter(code -> code != SiteCode.JUNGGYE).toList();

        for (SiteCode code : otherSites) {
            Long targetSiteId =
                    siteRepository.findByCode(code.name()).orElseThrow().getId();
            List<ChecklistItem> clones = sourceItems.stream()
                    .map(item -> {
                        ChecklistItem clone = new ChecklistItem(item.getTitle(), item.getCategory(), targetSiteId);
                        clone.setSortOrder(item.getSortOrder());
                        return clone;
                    })
                    .toList();
            checklistItemRepository.saveAll(clones);
        }
    }
}
