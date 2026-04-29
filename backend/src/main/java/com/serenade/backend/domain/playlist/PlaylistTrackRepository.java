package com.serenade.backend.domain.playlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, PlaylistTrackId> {
    List<PlaylistTrack> findByIdPlaylistIdOrderByPositionAsc(UUID playlistId);
    void deleteByIdPlaylistId(UUID playlistId);
    int countByIdPlaylistId(UUID playlistId);
}
