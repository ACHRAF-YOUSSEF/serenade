package com.serenade.backend.domain.subtitle.dto;

import com.serenade.backend.domain.subtitle.SubtitleLine;
import java.util.UUID;

public record SubtitleLineResponse(UUID id, long startMs, long endMs, String text) {
    public static SubtitleLineResponse from(SubtitleLine line) {
        return new SubtitleLineResponse(line.getId(), line.getStartMs(), line.getEndMs(), line.getText());
    }
}
