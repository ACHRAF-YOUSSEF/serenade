package com.serenade.backend.domain.track;

import com.serenade.backend.config.RabbitConfig;
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
import java.util.UUID;

@Service
public class TrackUploadService {

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

        Track track = new Track(title, artist, album, genre, uploader);
        track = tracks.save(track);

        String ext = extractExtension(file.getOriginalFilename());
        String rawKey = "raw/" + track.getId() + "." + ext;
        minio.uploadRaw(rawKey, file);

        rabbit.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.KEY_UPLOADED,
                Map.of("trackId", track.getId().toString(), "rawKey", rawKey));

        return new UploadResponse(track.getId(), track.getStatus().name());
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
