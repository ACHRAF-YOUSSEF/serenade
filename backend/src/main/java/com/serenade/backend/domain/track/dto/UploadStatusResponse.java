package com.serenade.backend.domain.track.dto;

import com.serenade.backend.domain.track.Genre;
import com.serenade.backend.domain.track.Track;
import com.serenade.backend.domain.track.TrackStatus;

import java.time.Instant;
import java.util.UUID;

public record UploadStatusResponse(
        UUID trackId,
        String title,
        String artist,
        String album,
        Genre genre,
        TrackStatus status,
        Long durationMs,
        String streamUrl,
        Instant updatedAt
) {
    public static UploadStatusResponse from(Track track) {
        return new UploadStatusResponse(
                track.getId(),
                track.getTitle(),
                track.getArtist(),
                track.getAlbum(),
                track.getGenre(),
                track.getStatus(),
                track.getDurationMs(),
                track.getStreamUrl(),
                track.getUpdatedAt()
        );
    }
}
