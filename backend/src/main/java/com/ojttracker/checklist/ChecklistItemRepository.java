package com.ojttracker.checklist;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    List<ChecklistItem> findByCategory(Category category);

    long countByCategory(Category category);

    @Query("SELECT c FROM ChecklistItem c ORDER BY c.sortOrder ASC NULLS LAST, c.id ASC")
    List<ChecklistItem> findAllSorted();
}
