package com.serenade.backend.domain.track;

import com.serenade.backend.domain.track.dto.TrackReadyRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/internal/tracks")
public class TrackInternalController {

    private final TrackRepository tracks;

    public TrackInternalController(TrackRepository tracks) {
        this.tracks = tracks;
    }

    @PostMapping("/{id}/ready")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void markReady(@PathVariable UUID id, @Valid @RequestBody TrackReadyRequest req) {
        Track track = tracks.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        track.markReady(req.streamUrl(), req.durationMs());
        tracks.save(track);
    }

    @PostMapping("/{id}/failed")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void markFailed(@PathVariable UUID id) {
        Track track = tracks.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        track.markFailed();
        tracks.save(track);
    }
}
