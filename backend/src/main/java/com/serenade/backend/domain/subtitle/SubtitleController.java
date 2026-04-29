package com.serenade.backend.domain.subtitle;

import com.serenade.backend.domain.subtitle.dto.SubtitleLineResponse;
import com.serenade.backend.domain.track.TrackRepository;
import com.serenade.backend.domain.track.TrackStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tracks")
public class SubtitleController {

    private final TrackRepository tracks;
    private final SubtitleLineRepository subtitles;

    public SubtitleController(TrackRepository tracks, SubtitleLineRepository subtitles) {
        this.tracks = tracks;
        this.subtitles = subtitles;
    }

    @GetMapping("/{id}/subtitles")
    public List<SubtitleLineResponse> getSubtitles(@PathVariable UUID id) {
        tracks.findByIdAndStatus(id, TrackStatus.READY)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return subtitles.findByTrackIdOrderByStartMsAsc(id)
                .stream().map(SubtitleLineResponse::from).toList();
    }
}
