package com.serenade.backend.domain.playlist.dto;

import com.serenade.backend.domain.playlist.Playlist;

import java.time.Instant;
import java.util.UUID;

public record PlaylistSummaryResponse(
        UUID id,
        String name,
        boolean isCopy,
        UUID sourcePlaylistId,
        int version,
        int trackCount,
        double ratingAvg,
        Instant updatedAt
) {
    public static PlaylistSummaryResponse from(Playlist p, int trackCount, double ratingAvg) {
        return new PlaylistSummaryResponse(
                p.getId(), p.getName(), p.isCopy(), p.getSourcePlaylistId(),
                p.getVersion(), trackCount, ratingAvg, p.getUpdatedAt());
    }
}
