package com.tup.youtube_playlist_checker.services;

import com.tup.youtube_playlist_checker.entity.Playlist;
import com.tup.youtube_playlist_checker.repositories.PlaylistRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public PlaylistService(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    public List<Playlist> obtenerTodas() {
        return playlistRepository.findAll();
    }

    public Optional<Playlist> obtenerPorId(Long id) {
        return playlistRepository.findById(id);
    }

    public Optional<Playlist> obtenerPorYoutubeId(String youtubeId) {
        return playlistRepository.findByYoutubeId(youtubeId);
    }

    public Playlist guardar(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    public void eliminar(Long id) {
        playlistRepository.deleteById(id);
    }
}