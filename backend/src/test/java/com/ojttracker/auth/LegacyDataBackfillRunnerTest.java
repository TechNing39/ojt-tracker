package com.ojttracker.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.ojttracker.checklist.Category;
import com.ojttracker.checklist.ChecklistItem;
import com.ojttracker.checklist.ChecklistItemRepository;
import com.ojttracker.trainee.Trainee;
import com.ojttracker.trainee.TraineeRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class LegacyDataBackfillRunnerTest {

    @Autowired
    private LegacyDataBackfillRunner runner;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private ChecklistItemRepository checklistItemRepository;

    @Test
    void backfillsLegacyDataToJunggyeAndClonesChecklistToOtherSites() {
        // siteId 컬럼 도입 이전(레거시)의 데이터를 흉내낸다: siteId가 null인 상태로 저장.
        ChecklistItem legacyItem = checklistItemRepository.save(new ChecklistItem("레거시항목", Category.FLOOR, null));
        Trainee legacyTrainee = traineeRepository.save(new Trainee("레거시신입", null));

        runner.run(new DefaultApplicationArguments());

        Long junggyeId =
                siteRepository.findByCode(SiteCode.JUNGGYE.name()).orElseThrow().getId();

        ChecklistItem backfilledItem =
                checklistItemRepository.findById(legacyItem.getId()).orElseThrow();
        assertThat(backfilledItem.getSiteId()).isEqualTo(junggyeId);

        Trainee backfilledTrainee =
                traineeRepository.findById(legacyTrainee.getId()).orElseThrow();
        assertThat(backfilledTrainee.getSiteId()).isEqualTo(junggyeId);

        for (SiteCode code : SiteCode.values()) {
            if (code == SiteCode.JUNGGYE) continue;
            Long siteId = siteRepository.findByCode(code.name()).orElseThrow().getId();
            List<ChecklistItem> cloned = checklistItemRepository.findAllSortedBySiteId(siteId);
            assertThat(cloned).hasSize(1);
            assertThat(cloned.get(0).getTitle()).isEqualTo("레거시항목");
            assertThat(cloned.get(0).getCategory()).isEqualTo(Category.FLOOR);
        }
    }
}
