package com.serenade.backend.domain.track;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/artwork")
public class TrackArtworkController {

    private static final Pattern SAFE_ARTWORK_KEY =
            Pattern.compile("artwork/[0-9a-fA-F-]{36}\\.(jpg|jpeg|png|webp)");

    private final TrackRepository tracks;
    private final MinioService minio;

    public TrackArtworkController(TrackRepository tracks, MinioService minio) {
        this.tracks = tracks;
        this.minio = minio;
    }

    @GetMapping("/{trackId}")
    public ResponseEntity<InputStreamResource> getArtwork(@PathVariable UUID trackId) {
        Track track = tracks.findById(trackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String objectKey = artworkObjectKey(track.getArtworkUrl())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        MinioService.ObjectData obj;
        try {
            obj = minio.getObject(objectKey);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artwork not found", e);
        }

        var builder = ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(mediaTypeFor(objectKey));
        if (obj.size() >= 0) {
            builder = builder.contentLength(obj.size());
        }
        return builder.body(new InputStreamResource(obj.stream()));
    }

    private static Optional<String> artworkObjectKey(String rawArtworkUrl) {
        if (rawArtworkUrl == null || rawArtworkUrl.isBlank()) {
            return Optional.empty();
        }
        String value = rawArtworkUrl.trim();
        int queryIndex = value.indexOf('?');
        String withoutQuery = queryIndex >= 0 ? value.substring(0, queryIndex) : value;
        if (withoutQuery.startsWith("artwork/") && SAFE_ARTWORK_KEY.matcher(withoutQuery).matches()) {
            return Optional.of(withoutQuery);
        }

        String path = runCatchingUriPath(withoutQuery).orElse(withoutQuery);
        int artworkIndex = path.indexOf("/artwork/");
        if (artworkIndex < 0) {
            return Optional.empty();
        }
        String objectKey = path.substring(artworkIndex + 1);
        return SAFE_ARTWORK_KEY.matcher(objectKey).matches()
                ? Optional.of(objectKey)
                : Optional.empty();
    }

    private static Optional<String> runCatchingUriPath(String value) {
        try {
            return Optional.ofNullable(URI.create(value).getPath());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static MediaType mediaTypeFor(String objectKey) {
        String lower = objectKey.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }
}
