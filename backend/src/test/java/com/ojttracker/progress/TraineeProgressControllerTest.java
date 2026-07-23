package com.ojttracker.progress;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ojttracker.auth.Role;
import com.ojttracker.auth.SiteCode;
import com.ojttracker.auth.SiteRepository;
import com.ojttracker.auth.TokenService;
import com.ojttracker.checklist.Category;
import com.ojttracker.checklist.ChecklistItem;
import com.ojttracker.checklist.ChecklistItemRepository;
import com.ojttracker.trainee.Trainee;
import com.ojttracker.trainee.TraineeRepository;
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
class TraineeProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private ChecklistItemRepository checklistItemRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SiteRepository siteRepository;

    private Long junggyeId;

    @BeforeEach
    void setUp() {
        junggyeId = siteRepository.findByCode(SiteCode.JUNGGYE.name()).orElseThrow().getId();
    }

    private String adminAuth() {
        return "Bearer " + tokenService.issueToken(Role.ADMIN, null);
    }

    @Test
    void listMergesCompletionStatusAndToggleFlips() throws Exception {
        Trainee trainee = traineeRepository.save(new Trainee("철수", junggyeId));
        ChecklistItem item = checklistItemRepository.save(new ChecklistItem("포스 사용법", Category.TICKETING, junggyeId));

        mockMvc.perform(get("/api/trainees/" + trainee.getId() + "/progress").header("Authorization", adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].completed").value(false));

        mockMvc.perform(patch("/api/trainees/" + trainee.getId() + "/progress/" + item.getId())
                        .header("Authorization", adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        mockMvc.perform(get("/api/trainees/" + trainee.getId() + "/progress").header("Authorization", adminAuth()))
                .andExpect(jsonPath("$[0].completed").value(true));

        mockMvc.perform(patch("/api/trainees/" + trainee.getId() + "/progress/" + item.getId())
                        .header("Authorization", adminAuth()))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void progressForNonexistentTraineeReturns404() throws Exception {
        mockMvc.perform(get("/api/trainees/9999/progress").header("Authorization", adminAuth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void toggleForNonexistentItemReturns404() throws Exception {
        Trainee trainee = traineeRepository.save(new Trainee("영희", junggyeId));
        mockMvc.perform(patch("/api/trainees/" + trainee.getId() + "/progress/9999")
                        .header("Authorization", adminAuth()))
                .andExpect(status().isNotFound());
    }
}
