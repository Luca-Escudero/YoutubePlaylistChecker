package com.tup.youtube_playlist_checker.mapper;

import com.tup.youtube_playlist_checker.dtos.ConsultaResponse;
import com.tup.youtube_playlist_checker.dtos.PlaylistResponse;
import com.tup.youtube_playlist_checker.dtos.VideoResponse;
import com.tup.youtube_playlist_checker.entity.Consulta;
import com.tup.youtube_playlist_checker.entity.Playlist;
import com.tup.youtube_playlist_checker.entity.Video;

import java.util.List;

public class EntityMapper {

    private EntityMapper() {}

    public static PlaylistResponse toPlaylistResponse(Playlist playlist) {

        if (playlist == null) {
            return null;
        }

        return new PlaylistResponse(
                playlist.getId(),
                playlist.getYoutubeId(),
                playlist.getUrl(),
                playlist.getTitulo(),
                playlist.getCantidadVideos(),
                playlist.getFechaPrimeraConsulta()
        );
    }

    public static VideoResponse toVideoResponse(Video video) {

        if (video == null) {
            return null;
        }

        return new VideoResponse(
                video.getId(),
                video.getYoutubeId(),
                video.getTitulo(),
                video.getEstado(),
                video.getMotivo(),
                video.getUltimaActualizacion()
        );
    }

    public static ConsultaResponse toConsultaResponse(Consulta consulta) {

        if (consulta == null) {
            return null;
        }

        Long playlistId = null;

        if (consulta.getPlaylist() != null) {
            playlistId = consulta.getPlaylist().getId();
        }

        return new ConsultaResponse(
                consulta.getId(),
                playlistId,
                consulta.getFechaConsulta(),
                consulta.getCantidadVideos(),
                consulta.getDisponibles(),
                consulta.getNoDisponibles()
        );
    }

    public static List<VideoResponse> toVideoResponseList(
            List<Video> videos) {

        return videos.stream()
                .map(EntityMapper::toVideoResponse)
                .toList();
    }
}
