package com.serenade.backend.domain.track;

import com.serenade.backend.domain.track.dto.UploadResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tracks")
public class TrackUploadController {

    private final TrackUploadService uploadService;

    public TrackUploadController(TrackUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UploadResponse upload(
            @RequestParam @NotBlank String title,
            @RequestParam @NotBlank String artist,
            @RequestParam(required = false) String album,
            @RequestParam @NotNull Genre genre,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        return uploadService.upload(auth.getName(), title, artist, album, genre, file);
    }
}
