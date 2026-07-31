package com.ojttracker.checklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ojttracker.auth.Role;
import com.ojttracker.auth.SiteCode;
import com.ojttracker.auth.SiteRepository;
import com.ojttracker.auth.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChecklistResyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ChecklistItemRepository checklistItemRepository;

    private Long junggyeId;
    private Long sangbongId;

    @BeforeEach
    void setUp() {
        junggyeId = siteRepository.findByCode(SiteCode.JUNGGYE.name()).orElseThrow().getId();
        sangbongId = siteRepository.findByCode(SiteCode.SANGBONG.name()).orElseThrow().getId();
    }

    private String adminAuth() {
        return "Bearer " + tokenService.issueToken(Role.ADMIN, null);
    }

    private String siteAuth(Long siteId) {
        return "Bearer " + tokenService.issueToken(Role.SITE, siteId);
    }

    @Test
    void resyncOverwritesOtherSiteChecklistWithJunggyes() throws Exception {
        ChecklistItem junggyeItem = new ChecklistItem("포스 사용법", Category.TICKETING, junggyeId);
        junggyeItem.setSortOrder(0);
        checklistItemRepository.save(junggyeItem);

        ChecklistItem sangbongOnlyItem = new ChecklistItem("상봉 전용 항목", Category.FLOOR, sangbongId);
        sangbongOnlyItem.setSortOrder(0);
        checklistItemRepository.save(sangbongOnlyItem);

        mockMvc.perform(post("/api/admin/checklist/resync-from-junggye").header("Authorization", adminAuth()))
                .andExpect(status().isOk());

        var sangbongItems = checklistItemRepository.findAllSortedBySiteId(sangbongId);
        assertThat(sangbongItems).hasSize(1);
        assertThat(sangbongItems.get(0).getTitle()).isEqualTo("포스 사용법");
        assertThat(sangbongItems.get(0).getCategory()).isEqualTo(Category.TICKETING);
    }

    @Test
    void resyncAsSiteRoleReturns403() throws Exception {
        mockMvc.perform(post("/api/admin/checklist/resync-from-junggye").header("Authorization", siteAuth(junggyeId)))
                .andExpect(status().isForbidden());
    }
}
