package com.serenade.backend.domain.subtitle;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SubtitleLineRepository extends JpaRepository<SubtitleLine, UUID> {
    List<SubtitleLine> findByTrackIdOrderByStartMsAsc(UUID trackId);
    void deleteByTrackId(UUID trackId);
}
