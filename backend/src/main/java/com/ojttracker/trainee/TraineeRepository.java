package com.ojttracker.trainee;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    List<Trainee> findBySiteId(Long siteId);

    Optional<Trainee> findByIdAndSiteId(Long id, Long siteId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Trainee t SET t.siteId = :siteId WHERE t.siteId IS NULL")
    void backfillSiteId(@Param("siteId") Long siteId);
}
