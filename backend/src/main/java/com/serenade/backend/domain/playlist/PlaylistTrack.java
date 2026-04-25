package com.serenade.backend.domain.playlist;

import com.serenade.backend.domain.track.Track;
import jakarta.persistence.*;

@Entity
@Table(name = "playlist_tracks")
public class PlaylistTrack {

    @EmbeddedId
    private PlaylistTrackId id;

    @Column(nullable = false)
    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("trackId")
    @JoinColumn(name = "track_id")
    private Track track;

    protected PlaylistTrack() {}

    public PlaylistTrack(PlaylistTrackId id, int position, Playlist playlist, Track track) {
        this.id = id;
        this.position = position;
        this.playlist = playlist;
        this.track = track;
    }

    public PlaylistTrackId getId() { return id; }
    public int getPosition() { return position; }
    public Playlist getPlaylist() { return playlist; }
    public Track getTrack() { return track; }
}
