package com.tup.youtube_playlist_checker.repositories;

import com.tup.youtube_playlist_checker.entity.Playlist;
import com.tup.youtube_playlist_checker.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository <Video, Long>{

    List<Video> findByPlaylist(Playlist playlist);
    Optional<Video> findByPlaylistAndYoutubeId(Playlist playlist, String youtubeId);
}
