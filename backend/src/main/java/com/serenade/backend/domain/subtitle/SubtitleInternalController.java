package com.serenade.backend.domain.subtitle;

import com.serenade.backend.domain.subtitle.dto.SubtitleLinePushRequest;
import com.serenade.backend.domain.track.Track;
import com.serenade.backend.domain.track.TrackRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/tracks")
public class SubtitleInternalController {

    private final TrackRepository tracks;
    private final SubtitleLineRepository subtitles;

    public SubtitleInternalController(TrackRepository tracks, SubtitleLineRepository subtitles) {
        this.tracks = tracks;
        this.subtitles = subtitles;
    }

    @PostMapping("/{id}/subtitles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void pushSubtitles(@PathVariable UUID id, @RequestBody @Valid List<@Valid SubtitleLinePushRequest> req) {
        Track track = tracks.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        subtitles.deleteByTrackId(id);
        List<SubtitleLine> lines = req.stream()
                .map(r -> new SubtitleLine(UUID.fromString(r.id()), track, r.startMs(), r.endMs(), r.text()))
                .toList();
        subtitles.saveAll(lines);
    }
}
