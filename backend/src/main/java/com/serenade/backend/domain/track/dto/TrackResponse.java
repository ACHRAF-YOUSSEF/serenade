package com.serenade.backend.domain.track.dto;

import com.serenade.backend.domain.track.Genre;
import com.serenade.backend.domain.track.Track;

import java.time.Instant;
import java.util.UUID;

public record TrackResponse(
        UUID id,
        String title,
        String artist,
        String album,
        Genre genre,
        Long durationMs,
        String artworkUrl,
        String streamUrl,
        Instant updatedAt
) {
    public static TrackResponse from(Track t) {
        return new TrackResponse(
                t.getId(), t.getTitle(), t.getArtist(), t.getAlbum(),
                t.getGenre(), t.getDurationMs(), t.getArtworkUrl(), t.getStreamUrl(),
                t.getUpdatedAt()
        );
    }
}
