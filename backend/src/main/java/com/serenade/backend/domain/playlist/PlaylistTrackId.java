package com.serenade.backend.domain.playlist;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PlaylistTrackId implements Serializable {

    @Column(name = "playlist_id")
    private UUID playlistId;

    @Column(name = "track_id")
    private UUID trackId;

    protected PlaylistTrackId() {}

    public PlaylistTrackId(UUID playlistId, UUID trackId) {
        this.playlistId = playlistId;
        this.trackId = trackId;
    }

    public UUID getPlaylistId() { return playlistId; }
    public UUID getTrackId() { return trackId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaylistTrackId that)) return false;
        return Objects.equals(playlistId, that.playlistId) && Objects.equals(trackId, that.trackId);
    }

    @Override
    public int hashCode() { return Objects.hash(playlistId, trackId); }
}
