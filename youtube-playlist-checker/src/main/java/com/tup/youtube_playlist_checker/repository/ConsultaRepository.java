package com.tup.youtube_playlist_checker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tup.youtube_playlist_checker.entity.Playlist;

@Repository
public interface ConsultaRepository extends JpaRepository<Playlist, Long> {

}


