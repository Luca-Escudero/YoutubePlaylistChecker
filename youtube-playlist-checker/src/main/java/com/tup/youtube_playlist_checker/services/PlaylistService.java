package com.tup.youtube_playlist_checker.services;

import com.tup.youtube_playlist_checker.entity.Playlist;
import com.tup.youtube_playlist_checker.repositories.PlaylistRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final YoutubeService youtubeService;

    public PlaylistService(PlaylistRepository playlistRepository, YoutubeService youtubeService) {
        this.playlistRepository = playlistRepository;
        this.youtubeService = youtubeService;
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

    public Playlist obtenerOCrearDesdeYoutube(String youtubeId) {

        // Buscamos en BD
        Optional<Playlist> playlistExistente = playlistRepository.findByYoutubeId(youtubeId);

        if (playlistExistente.isPresent()) {
            return playlistExistente.get();
        }

        // Si no existe, consultamos YouTube
        YoutubeService.YoutubePlaylist playlistYoutube = youtubeService.obtenerPlaylist(youtubeId);

        if (playlistYoutube == null) {
            return null;
        }

        // Creamos nuestra entidad
        Playlist playlist = new Playlist();

        playlist.setYoutubeId(playlistYoutube.youtubeId());
        playlist.setTitulo(playlistYoutube.titulo());
        playlist.setCantidadVideos(playlistYoutube.cantidadVideos());

        playlist.setUrl(
                "https://www.youtube.com/playlist?list=" + youtubeId
        );

        playlist.setFechaPrimeraConsulta(LocalDateTime.now());

        // Guardamos en BD
        return playlistRepository.save(playlist);
    }

    public Playlist guardar(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    public void eliminar(Long id) {
        playlistRepository.deleteById(id);
    }
}