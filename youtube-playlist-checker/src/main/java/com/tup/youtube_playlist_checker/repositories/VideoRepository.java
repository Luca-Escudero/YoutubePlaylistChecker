package com.tup.youtube_playlist_checker.repositories;

import com.tup.youtube_playlist_checker.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository <Video, Long>{

    List<Video> findByPlaylistId(Long playlistId);
    Optional<Video> findByPlaylistIdAndYoutubeId(Long playlistId, String youtubeId);
}
