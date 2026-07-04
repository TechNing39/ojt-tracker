package com.ojttracker.dashboard;

import com.ojttracker.checklist.Category;
import com.ojttracker.checklist.ChecklistItem;
import com.ojttracker.checklist.ChecklistItemRepository;
import com.ojttracker.progress.TraineeProgress;
import com.ojttracker.progress.TraineeProgressRepository;
import com.ojttracker.trainee.Trainee;
import com.ojttracker.trainee.TraineeRepository;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TraineeRepository traineeRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final TraineeProgressRepository progressRepository;

    public DashboardController(
            TraineeRepository traineeRepository,
            ChecklistItemRepository checklistItemRepository,
            TraineeProgressRepository progressRepository) {
        this.traineeRepository = traineeRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.progressRepository = progressRepository;
    }

    @GetMapping("/trainees")
    public List<TraineeSummaryView> trainees() {
        List<ChecklistItem> items = checklistItemRepository.findAll();
        Map<Long, Category> categoryByItemId =
                items.stream().collect(Collectors.toMap(ChecklistItem::getId, ChecklistItem::getCategory));
        Map<Category, Long> totalByCategory =
                items.stream().collect(Collectors.groupingBy(ChecklistItem::getCategory, Collectors.counting()));
        int totalItems = items.size();

        List<Trainee> trainees = traineeRepository.findAll();

        return trainees.stream()
                .map(trainee -> {
                    List<TraineeProgress> completed = progressRepository.findByTraineeId(trainee.getId()).stream()
                            .filter(TraineeProgress::isCompleted)
                            .toList();

                    Map<Category, Long> completedByCategory = completed.stream()
                            .collect(Collectors.groupingBy(
                                    p -> categoryByItemId.get(p.getChecklistItemId()), Collectors.counting()));

                    Map<Category, CategoryCount> byCategory = new EnumMap<>(Category.class);
                    for (Category category : Category.values()) {
                        int categoryCompleted = completedByCategory.getOrDefault(category, 0L).intValue();
                        int categoryTotal = totalByCategory.getOrDefault(category, 0L).intValue();
                        byCategory.put(category, new CategoryCount(categoryCompleted, categoryTotal));
                    }

                    return new TraineeSummaryView(
                            trainee.getId(), trainee.getName(), completed.size(), totalItems, byCategory);
                })
                .toList();
    }
}
