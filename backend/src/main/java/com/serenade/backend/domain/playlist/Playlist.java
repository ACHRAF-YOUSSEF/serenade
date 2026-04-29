package com.serenade.backend.domain.playlist;

import com.serenade.backend.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "is_copy", nullable = false)
    private boolean copy;

    @Column(name = "source_playlist_id")
    private UUID sourcePlaylistId;

    @Version
    private int version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Playlist() {}

    public Playlist(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.copy = false;
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }

    public void rename(String name) { this.name = name; }

    public void markAsCopy(UUID sourceId) {
        this.copy = true;
        this.sourcePlaylistId = sourceId;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public User getOwner() { return owner; }
    public boolean isCopy() { return copy; }
    public UUID getSourcePlaylistId() { return sourcePlaylistId; }
    public int getVersion() { return version; }
    public Instant getUpdatedAt() { return updatedAt; }
}
