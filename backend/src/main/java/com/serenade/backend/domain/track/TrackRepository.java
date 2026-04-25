package com.serenade.backend.domain.track;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {
    Page<Track> findByStatus(TrackStatus status, Pageable pageable);
    Optional<Track> findByIdAndStatus(UUID id, TrackStatus status);
    Optional<Track> findByIdAndUploader_Id(UUID id, UUID uploaderId);
    List<Track> findByStatusAndUpdatedAtAfterOrderByUpdatedAtAsc(TrackStatus status, Instant since, Pageable pageable);

    @Query(value = """
            SELECT * FROM tracks
            WHERE status = 'READY'
            AND (:genre IS NULL OR genre = :genre)
            AND (:q = '' OR search_vector @@ plainto_tsquery('simple', :q))
            ORDER BY
                CASE WHEN :q <> '' THEN ts_rank(search_vector, plainto_tsquery('simple', :q)) ELSE 0 END DESC,
                updated_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM tracks
            WHERE status = 'READY'
            AND (:genre IS NULL OR genre = :genre)
            AND (:q = '' OR search_vector @@ plainto_tsquery('simple', :q))
            """,
            nativeQuery = true)
    Page<Track> search(@Param("q") String q, @Param("genre") String genre, Pageable pageable);
}
