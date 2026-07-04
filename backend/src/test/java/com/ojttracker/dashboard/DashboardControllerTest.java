package com.ojttracker.dashboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ojttracker.checklist.Category;
import com.ojttracker.checklist.ChecklistItem;
import com.ojttracker.checklist.ChecklistItemRepository;
import com.ojttracker.progress.TraineeProgress;
import com.ojttracker.progress.TraineeProgressRepository;
import com.ojttracker.trainee.Trainee;
import com.ojttracker.trainee.TraineeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private ChecklistItemRepository checklistItemRepository;

    @Autowired
    private TraineeProgressRepository progressRepository;

    @Test
    void aggregatesCompletionByCategory() throws Exception {
        Trainee trainee = traineeRepository.save(new Trainee("철수"));
        ChecklistItem floor1 = checklistItemRepository.save(new ChecklistItem("상영관 안내", Category.FLOOR));
        ChecklistItem floor2 = checklistItemRepository.save(new ChecklistItem("청소", Category.FLOOR));
        checklistItemRepository.save(new ChecklistItem("포스 사용법", Category.TICKETING));

        TraineeProgress p1 = new TraineeProgress(trainee.getId(), floor1.getId());
        p1.toggle();
        progressRepository.save(p1);
        TraineeProgress p2 = new TraineeProgress(trainee.getId(), floor2.getId());
        p2.toggle();
        progressRepository.save(p2);

        mockMvc.perform(get("/api/dashboard/trainees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].completedTotal").value(2))
                .andExpect(jsonPath("$[0].totalItems").value(3))
                .andExpect(jsonPath("$[0].byCategory.FLOOR.completed").value(2))
                .andExpect(jsonPath("$[0].byCategory.FLOOR.total").value(2))
                .andExpect(jsonPath("$[0].byCategory.TICKETING.completed").value(0))
                .andExpect(jsonPath("$[0].byCategory.TICKETING.total").value(1));
    }
}
