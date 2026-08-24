package com.tup.youtube_playlist_checker.repositories;

import com.tup.youtube_playlist_checker.entity.Playlist;
import com.tup.youtube_playlist_checker.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository <Playlist, Long>{

    Optional<Playlist> findById(Long id);
    Optional<Playlist> findByYoutubeId(String youtubeId);
}
