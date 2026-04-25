package com.serenade.backend.domain.playlist;

import com.serenade.backend.domain.playlist.dto.*;
import com.serenade.backend.domain.rating.RatingRepository;
import com.serenade.backend.domain.track.Track;
import com.serenade.backend.domain.track.TrackRepository;
import com.serenade.backend.domain.track.dto.TrackResponse;
import com.serenade.backend.domain.user.User;
import com.serenade.backend.domain.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class PlaylistService {

    private final PlaylistRepository playlists;
    private final PlaylistTrackRepository playlistTracks;
    private final TrackRepository tracks;
    private final UserRepository users;
    private final RatingRepository ratings;

    public PlaylistService(PlaylistRepository playlists, PlaylistTrackRepository playlistTracks,
                           TrackRepository tracks, UserRepository users, RatingRepository ratings) {
        this.playlists = playlists;
        this.playlistTracks = playlistTracks;
        this.tracks = tracks;
        this.users = users;
        this.ratings = ratings;
    }

    @Transactional(readOnly = true)
    public Page<PlaylistSummaryResponse> list(UUID userId, Pageable pageable) {
        return playlists.findByOwnerIdOrderByUpdatedAtDesc(userId, pageable)
                .map(p -> {
                    int count = playlistTracks.countByIdPlaylistId(p.getId());
                    double avg = avgRating(p.getId());
                    return PlaylistSummaryResponse.from(p, count, avg);
                });
    }

    @Transactional(readOnly = true)
    public PlaylistDetailResponse getDetail(UUID playlistId, UUID userId) {
        Playlist p = findOrThrow(playlistId);
        assertOwner(p, userId);
        List<TrackResponse> trackList = playlistTracks.findByIdPlaylistIdOrderByPositionAsc(playlistId)
                .stream().map(pt -> TrackResponse.from(pt.getTrack())).toList();
        return new PlaylistDetailResponse(p.getId(), p.getName(), p.isCopy(), p.getSourcePlaylistId(),
                p.getVersion(), avgRating(playlistId), trackList, p.getUpdatedAt());
    }

    @Transactional
    public PlaylistSummaryResponse create(UUID userId, CreatePlaylistRequest req) {
        User owner = users.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Playlist p = new Playlist(req.name(), owner);
        playlists.save(p);
        return PlaylistSummaryResponse.from(p, 0, 0.0);
    }

    @Transactional
    public PlaylistSummaryResponse update(UUID playlistId, UUID userId, UpdatePlaylistRequest req) {
        Playlist p = findOrThrow(playlistId);
        assertOwner(p, userId);
        if (p.getVersion() != req.version()) throw new ResponseStatusException(HttpStatus.CONFLICT);
        p.rename(req.name());
        playlists.save(p);
        int count = playlistTracks.countByIdPlaylistId(playlistId);
        return PlaylistSummaryResponse.from(p, count, avgRating(playlistId));
    }

    @Transactional
    public void setTracks(UUID playlistId, UUID userId, List<TrackPositionRequest> req) {
        Playlist p = findOrThrow(playlistId);
        assertOwner(p, userId);
        assertUniqueTracksAndPositions(req);
        playlistTracks.deleteByIdPlaylistId(playlistId);
        List<PlaylistTrack> lines = req.stream().map(r -> {
            Track t = tracks.findById(r.trackId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            return new PlaylistTrack(new PlaylistTrackId(playlistId, r.trackId()), r.position(), p, t);
        }).toList();
        playlistTracks.saveAll(lines);
    }

    @Transactional
    public PlaylistSummaryResponse copy(UUID playlistId, UUID userId) {
        Playlist src = findOrThrow(playlistId);
        assertOwner(src, userId);
        User owner = users.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Playlist copy = new Playlist(src.getName() + " Copy", owner);
        copy.markAsCopy(src.getId());
        playlists.save(copy);
        List<PlaylistTrack> srcTracks = playlistTracks.findByIdPlaylistIdOrderByPositionAsc(playlistId);
        List<PlaylistTrack> copyTracks = srcTracks.stream()
                .map(pt -> new PlaylistTrack(
                        new PlaylistTrackId(copy.getId(), pt.getId().getTrackId()),
                        pt.getPosition(), copy, pt.getTrack()))
                .toList();
        playlistTracks.saveAll(copyTracks);
        return PlaylistSummaryResponse.from(copy, copyTracks.size(), 0.0);
    }

    @Transactional
    public void delete(UUID playlistId, UUID userId) {
        Playlist p = findOrThrow(playlistId);
        assertOwner(p, userId);
        playlists.delete(p);
    }

    private Playlist findOrThrow(UUID id) {
        return playlists.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void assertOwner(Playlist p, UUID userId) {
        if (!p.getOwner().getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private void assertUniqueTracksAndPositions(List<TrackPositionRequest> req) {
        Set<UUID> trackIds = new HashSet<>();
        Set<Integer> positions = new HashSet<>();
        for (TrackPositionRequest item : req) {
            if (!trackIds.add(item.trackId()) || !positions.add(item.position())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }
    }

    private double avgRating(UUID playlistId) {
        Double avg = ratings.findAvgByTargetTypeAndTargetId("PLAYLIST", playlistId);
        return avg != null ? avg : 0.0;
    }
}
