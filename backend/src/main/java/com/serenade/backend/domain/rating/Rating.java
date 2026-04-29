package com.serenade.backend.domain.rating;

import com.serenade.backend.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_type", nullable = false, length = 16)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(nullable = false)
    private int value;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Rating() {}

    public Rating(User user, String targetType, UUID targetId, int value) {
        this.user = user;
        this.targetType = targetType;
        this.targetId = targetId;
        this.value = value;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void setValue(int value) {
        this.value = value;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getTargetType() { return targetType; }
    public UUID getTargetId() { return targetId; }
    public int getValue() { return value; }
    public Instant getUpdatedAt() { return updatedAt; }
}
