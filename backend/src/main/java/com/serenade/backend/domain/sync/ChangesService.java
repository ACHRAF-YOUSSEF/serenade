package com.serenade.backend.domain.sync;

import com.serenade.backend.domain.playlist.PlaylistTrackRepository;
import com.serenade.backend.domain.playlist.dto.PlaylistSummaryResponse;
import com.serenade.backend.domain.playlist.PlaylistRepository;
import com.serenade.backend.domain.rating.RatingRepository;
import com.serenade.backend.domain.rating.dto.RatingResponse;
import com.serenade.backend.domain.sync.dto.ChangesResponse;
import com.serenade.backend.domain.track.TrackRepository;
import com.serenade.backend.domain.track.TrackStatus;
import com.serenade.backend.domain.track.dto.TrackResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ChangesService {

    private final TrackRepository tracks;
    private final PlaylistRepository playlists;
    private final PlaylistTrackRepository playlistTracks;
    private final RatingRepository ratings;

    public ChangesService(
            TrackRepository tracks,
            PlaylistRepository playlists,
            PlaylistTrackRepository playlistTracks,
            RatingRepository ratings
    ) {
        this.tracks = tracks;
        this.playlists = playlists;
        this.playlistTracks = playlistTracks;
        this.ratings = ratings;
    }

    @Transactional(readOnly = true)
    public ChangesResponse changes(UUID userId, Instant since, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        List<TrackResponse> changedTracks = tracks
                .findByStatusAndUpdatedAtAfterOrderByUpdatedAtAsc(TrackStatus.READY, since, page)
                .stream()
                .map(TrackResponse::from)
                .toList();

        List<PlaylistSummaryResponse> changedPlaylists = playlists
                .findByOwnerIdAndUpdatedAtAfterOrderByUpdatedAtAsc(userId, since, page)
                .stream()
                .map(p -> PlaylistSummaryResponse.from(p, playlistTracks.countByIdPlaylistId(p.getId()), avgFor("PLAYLIST", p.getId())))
                .toList();

        List<RatingResponse> changedRatings = ratings
                .findByUserIdAndUpdatedAtAfterOrderByUpdatedAtAsc(userId, since, page)
                .stream()
                .map(r -> new RatingResponse(r.getId(), r.getTargetType(), r.getTargetId(), r.getValue(),
                        avgFor(r.getTargetType(), r.getTargetId()), r.getUpdatedAt()))
                .toList();

        Instant nextCursor = Stream.of(
                        changedTracks.stream().map(TrackResponse::updatedAt),
                        changedPlaylists.stream().map(PlaylistSummaryResponse::updatedAt),
                        changedRatings.stream().map(RatingResponse::updatedAt)
                )
                .flatMap(s -> s)
                .max(Comparator.naturalOrder())
                .orElse(since);

        return new ChangesResponse(changedTracks, changedPlaylists, changedRatings, nextCursor);
    }

    private double avgFor(String targetType, UUID targetId) {
        Double avg = ratings.findAvgByTargetTypeAndTargetId(targetType, targetId);
        return avg != null ? avg : 0.0;
    }
}
