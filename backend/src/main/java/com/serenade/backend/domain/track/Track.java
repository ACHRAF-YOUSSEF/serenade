package com.serenade.backend.domain.track;

import com.serenade.backend.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String artist;

    @Column(length = 255)
    private String album;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Genre genre;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "artwork_url")
    private String artworkUrl;

    @Column(name = "stream_url")
    private String streamUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TrackStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private User uploader;

    @Version
    private int version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Track() {}

    public Track(String title, String artist, String album, Genre genre, User uploader) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.uploader = uploader;
        this.status = TrackStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public Genre getGenre() { return genre; }
    public Long getDurationMs() { return durationMs; }
    public String getArtworkUrl() { return artworkUrl; }
    public String getStreamUrl() { return streamUrl; }
    public TrackStatus getStatus() { return status; }
    public User getUploader() { return uploader; }
    public int getVersion() { return version; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void markReady(String streamUrl, long durationMs) {
        this.streamUrl = streamUrl;
        this.durationMs = durationMs;
        this.status = TrackStatus.READY;
    }

    public void markFailed() {
        this.status = TrackStatus.FAILED;
    }
}
