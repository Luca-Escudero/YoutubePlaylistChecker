package com.tup.youtube_playlist_checker.services;

import com.tup.youtube_playlist_checker.entity.EstadoVideo;
import com.tup.youtube_playlist_checker.entity.MotivoIndisponibilidad;
import com.tup.youtube_playlist_checker.entity.Playlist;
import com.tup.youtube_playlist_checker.entity.Video;
import com.tup.youtube_playlist_checker.repositories.VideoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VideoService {

    private final VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public List<Video> obtenerTodos() {
        return videoRepository.findAll();
    }

    public Optional<Video> obtenerPorId(Long id) {
        return videoRepository.findById(id);
    }

    public List<Video> obtenerPorPlaylist(Playlist playlist) {
        return videoRepository.findByPlaylist(playlist);
    }

    public Optional<Video> obtenerPorPlaylistYYoutubeId(
            Playlist playlist,
            String youtubeId) {

        return videoRepository.findByPlaylistAndYoutubeId(
                playlist,
                youtubeId
        );
    }

    public Video guardar(Video video) {
        return videoRepository.save(video);
    }

    public Video crearOActualizar(
            Playlist playlist,
            String youtubeId,
            String titulo,
            EstadoVideo estado,
            MotivoIndisponibilidad motivo) {

        Optional<Video> videoExistente =
                videoRepository.findByPlaylistAndYoutubeId(
                        playlist,
                        youtubeId
                );

        Video video;

        if (videoExistente.isPresent()) {
            video = videoExistente.get();
        } else {
            video = new Video();
            video.setPlaylist(playlist);
            video.setYoutubeId(youtubeId);
        }

        video.setTitulo(titulo);
        video.setEstado(estado);
        video.setMotivo(motivo);
        video.setUltimaActualizacion(LocalDateTime.now());

        return videoRepository.save(video);
    }

    public void eliminar(Long id) {
        videoRepository.deleteById(id);
    }
}
