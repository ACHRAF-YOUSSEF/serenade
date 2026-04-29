package com.serenade.backend.domain.track;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/hls")
public class HlsController {

    private static final Pattern SAFE_HLS_FILE =
            Pattern.compile("(index\\.m3u8|seg\\d{3}\\.ts)");
    private static final MediaType HLS_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.apple.mpegurl");
    private static final MediaType TS_MEDIA_TYPE =
            MediaType.parseMediaType("video/mp2t");

    private final MinioService minio;

    public HlsController(MinioService minio) {
        this.minio = minio;
    }

    @GetMapping("/{trackId}/{fileName}")
    public ResponseEntity<Resource> getHlsFile(
            @PathVariable UUID trackId,
            @PathVariable String fileName
    ) {
        if (!SAFE_HLS_FILE.matcher(fileName).matches()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        String objectKey = "hls/%s/%s".formatted(trackId, fileName);
        MinioService.ObjectData obj;
        try {
            obj = minio.getObject(objectKey);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "HLS file not found", e);
        }

        if (fileName.endsWith(".m3u8")) {
            byte[] manifestBytes = normalizedVodManifest(obj);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .contentType(HLS_MEDIA_TYPE)
                    .contentLength(manifestBytes.length)
                    .body(new ByteArrayResource(manifestBytes));
        }

        var builder = ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(TS_MEDIA_TYPE);
        if (obj.size() >= 0) {
            builder = builder.contentLength(obj.size());
        }
        return builder.body(new InputStreamResource(obj.stream()));
    }

    private byte[] normalizedVodManifest(MinioService.ObjectData obj) {
        try (var stream = obj.stream()) {
            String manifest = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n");
            if (!manifest.contains("#EXT-X-PLAYLIST-TYPE:")) {
                manifest = insertAfterHeader(manifest, "#EXT-X-PLAYLIST-TYPE:VOD\n");
            }
            if (!manifest.contains("#EXT-X-ENDLIST")) {
                manifest = manifest.stripTrailing() + "\n#EXT-X-ENDLIST\n";
            }
            return manifest.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "HLS manifest not readable", e);
        }
    }

    private String insertAfterHeader(String manifest, String line) {
        int versionIndex = manifest.indexOf("#EXT-X-VERSION:");
        if (versionIndex >= 0) {
            int lineEnd = manifest.indexOf('\n', versionIndex);
            if (lineEnd >= 0) {
                return manifest.substring(0, lineEnd + 1) + line + manifest.substring(lineEnd + 1);
            }
        }

        String prefix = "#EXTM3U\n";

        if (manifest.startsWith(prefix)) {
            return prefix + line + manifest.substring(prefix.length());
        }
        return line + manifest;
    }
}
