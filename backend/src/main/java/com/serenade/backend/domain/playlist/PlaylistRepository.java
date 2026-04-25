package com.serenade.backend.domain.playlist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {
    Page<Playlist> findByOwnerIdOrderByUpdatedAtDesc(UUID ownerId, Pageable pageable);
}
