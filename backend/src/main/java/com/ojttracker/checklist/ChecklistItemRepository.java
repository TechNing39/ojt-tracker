package com.ojttracker.checklist;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    List<ChecklistItem> findByCategoryAndSiteId(Category category, Long siteId);

    long countByCategoryAndSiteId(Category category, Long siteId);

    Optional<ChecklistItem> findByIdAndSiteId(Long id, Long siteId);

    @Query("SELECT c FROM ChecklistItem c WHERE c.siteId = :siteId ORDER BY c.sortOrder ASC NULLS LAST, c.id ASC")
    List<ChecklistItem> findAllSortedBySiteId(@Param("siteId") Long siteId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ChecklistItem c SET c.siteId = :siteId WHERE c.siteId IS NULL")
    int backfillSiteId(@Param("siteId") Long siteId);
}
