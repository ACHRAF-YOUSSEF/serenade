package com.serenade.backend.domain.rating;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {

    Optional<Rating> findByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

    @Query("SELECT AVG(r.value) FROM Rating r WHERE r.targetType = :type AND r.targetId = :id")
    Double findAvgByTargetTypeAndTargetId(@Param("type") String targetType, @Param("id") UUID targetId);

    List<Rating> findByUserIdAndUpdatedAtAfterOrderByUpdatedAtAsc(UUID userId, Instant since, Pageable pageable);
}
