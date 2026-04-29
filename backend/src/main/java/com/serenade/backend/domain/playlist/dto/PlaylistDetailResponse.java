package com.serenade.backend.domain.playlist.dto;

import com.serenade.backend.domain.track.dto.TrackResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaylistDetailResponse(
        UUID id,
        String name,
        boolean isCopy,
        UUID sourcePlaylistId,
        int version,
        double ratingAvg,
        List<TrackResponse> tracks,
        Instant updatedAt
) {}
