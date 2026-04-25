package com.serenade.backend.domain.track;

import com.serenade.backend.config.RabbitConfig;
import com.serenade.backend.domain.track.dto.UploadStatusResponse;
import com.serenade.backend.domain.track.dto.UploadResponse;
import com.serenade.backend.domain.user.User;
import com.serenade.backend.domain.user.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class TrackUploadService {
    private static final Set<String> ALLOWED_AUDIO_EXTENSIONS = Set.of(
            "mp3", "flac", "ogg", "wav", "m4a", "aac"
    );

    private final TrackRepository tracks;
    private final UserRepository users;
    private final MinioService minio;
    private final RabbitTemplate rabbit;

    public TrackUploadService(TrackRepository tracks, UserRepository users,
                               MinioService minio, RabbitTemplate rabbit) {
        this.tracks = tracks;
        this.users = users;
        this.minio = minio;
        this.rabbit = rabbit;
    }

    @Transactional
    public UploadResponse upload(String uploaderId, String title, String artist,
                                  String album, Genre genre, MultipartFile file) {
        User uploader = users.findById(UUID.fromString(uploaderId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        validateUploadFile(file);

        Track track = new Track(title, artist, album, genre, uploader);
        track = tracks.save(track);

        String rawKey = "raw/" + track.getId();
        minio.uploadRaw(rawKey, file);

        rabbit.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.KEY_UPLOADED,
                Map.of("trackId", track.getId().toString(), "rawKey", rawKey));

        return new UploadResponse(track.getId(), track.getStatus().name());
    }

    @Transactional(readOnly = true)
    public UploadStatusResponse status(String uploaderId, UUID trackId) {
        UUID userId = UUID.fromString(uploaderId);
        return tracks.findByIdAndUploader_Id(trackId, userId)
                .map(UploadStatusResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void validateUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String filename = file.getOriginalFilename();
        String extension = extractExtension(filename);
        if (!ALLOWED_AUDIO_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String normalized = contentType.toLowerCase(Locale.ROOT);
            boolean looksLikeAudio = normalized.startsWith("audio/")
                    || normalized.equals("application/octet-stream")
                    || normalized.equals("video/mp4")
                    || normalized.equals("video/x-m4v");
            if (!looksLikeAudio) {
                throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
