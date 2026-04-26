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
    public ResponseEntity<InputStreamResource> getHlsFile(
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

        MediaType mediaType = fileName.endsWith(".m3u8") ? HLS_MEDIA_TYPE : TS_MEDIA_TYPE;
        var builder = ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(mediaType);
        if (obj.size() >= 0) {
            builder = builder.contentLength(obj.size());
        }
        return builder.body(new InputStreamResource(obj.stream()));
    }
}
