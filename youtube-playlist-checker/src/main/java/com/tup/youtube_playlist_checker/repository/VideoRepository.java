package com.tup.youtube_playlist_checker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tup.youtube_playlist_checker.entity.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

}

