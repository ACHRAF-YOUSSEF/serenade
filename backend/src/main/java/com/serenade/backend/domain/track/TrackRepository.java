package com.serenade.backend.domain.track;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {
    Page<Track> findByStatus(TrackStatus status, Pageable pageable);
    Optional<Track> findByIdAndStatus(UUID id, TrackStatus status);
}
