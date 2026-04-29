package com.serenade.backend.domain.subtitle;

import com.serenade.backend.domain.track.Track;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subtitle_lines")
public class SubtitleLine {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Column(name = "start_ms", nullable = false)
    private long startMs;

    @Column(name = "end_ms", nullable = false)
    private long endMs;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SubtitleLine() {}

    public SubtitleLine(UUID id, Track track, long startMs, long endMs, String text) {
        this.id = id;
        this.track = track;
        this.startMs = startMs;
        this.endMs = endMs;
        this.text = text;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Track getTrack() { return track; }
    public long getStartMs() { return startMs; }
    public long getEndMs() { return endMs; }
    public String getText() { return text; }
}
