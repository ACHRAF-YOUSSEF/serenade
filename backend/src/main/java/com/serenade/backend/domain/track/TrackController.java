package com.serenade.backend.domain.track;

import com.serenade.backend.domain.track.dto.TrackResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    private final TrackRepository tracks;

    public TrackController(TrackRepository tracks) {
        this.tracks = tracks;
    }

    @GetMapping
    public Page<TrackResponse> list(@PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {
        return tracks.findByStatus(TrackStatus.READY, pageable).map(TrackResponse::from);
    }

    @GetMapping("/{id}")
    public TrackResponse get(@PathVariable UUID id) {
        return tracks.findByIdAndStatus(id, TrackStatus.READY)
                .map(TrackResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
