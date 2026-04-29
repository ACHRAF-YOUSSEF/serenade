package com.serenade.backend.domain.track;

import com.serenade.backend.domain.track.dto.TrackResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final TrackRepository tracks;

    public SearchController(TrackRepository tracks) {
        this.tracks = tracks;
    }

    @GetMapping
    public Page<TrackResponse> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String genres,
            @PageableDefault(size = 20) Pageable pageable) {
        return tracks.search(q, genres, pageable).map(TrackResponse::from);
    }
}
