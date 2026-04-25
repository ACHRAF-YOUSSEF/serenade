package com.serenade.backend.domain.playlist;

import com.serenade.backend.domain.playlist.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService service;

    public PlaylistController(PlaylistService service) {
        this.service = service;
    }

    @GetMapping
    public Page<PlaylistSummaryResponse> list(Authentication auth,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.list(UUID.fromString(auth.getName()), pageable);
    }

    @GetMapping("/{id}")
    public PlaylistDetailResponse get(@PathVariable UUID id) {
        return service.getDetail(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistSummaryResponse create(@Valid @RequestBody CreatePlaylistRequest req,
            Authentication auth) {
        return service.create(UUID.fromString(auth.getName()), req);
    }

    @PatchMapping("/{id}")
    public PlaylistSummaryResponse update(@PathVariable UUID id,
            @Valid @RequestBody UpdatePlaylistRequest req, Authentication auth) {
        return service.update(id, UUID.fromString(auth.getName()), req);
    }

    @PutMapping("/{id}/tracks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setTracks(@PathVariable UUID id,
            @RequestBody @Valid List<@Valid TrackPositionRequest> req, Authentication auth) {
        service.setTracks(id, UUID.fromString(auth.getName()), req);
    }

    @PostMapping("/{id}/copy")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistSummaryResponse copy(@PathVariable UUID id, Authentication auth) {
        return service.copy(id, UUID.fromString(auth.getName()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, Authentication auth) {
        service.delete(id, UUID.fromString(auth.getName()));
    }
}
