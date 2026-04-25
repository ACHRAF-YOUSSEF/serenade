package com.serenade.backend.domain.sync.dto;

import com.serenade.backend.domain.playlist.dto.PlaylistSummaryResponse;
import com.serenade.backend.domain.rating.dto.RatingResponse;
import com.serenade.backend.domain.track.dto.TrackResponse;

import java.time.Instant;
import java.util.List;

public record ChangesResponse(
        List<TrackResponse> tracks,
        List<PlaylistSummaryResponse> playlists,
        List<RatingResponse> ratings,
        Instant nextCursor
) {}
