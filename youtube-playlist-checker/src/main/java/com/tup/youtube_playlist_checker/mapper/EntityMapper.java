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
                video.getCanal(),
                video.getEstado(),
                video.getEstado() == com.tup.youtube_playlist_checker.entity.EstadoVideo.DISPONIBLE,
                video.getMotivo(),
                video.getUltimaActualizacion()
        );
    }

    public static ConsultaResponse toConsultaResponse(Consulta consulta) {
        return toConsultaResponse(consulta, null);
    }

    public static ConsultaResponse toConsultaResponse(Consulta consulta, List<Video> videos) {

        if (consulta == null) {
            return null;
        }

        Long playlistId = null;
        PlaylistResponse playlistResponse = null;

        if (consulta.getPlaylist() != null) {
            playlistId = consulta.getPlaylist().getId();
            playlistResponse = toPlaylistResponse(consulta.getPlaylist());
        }

        List<VideoResponse> videoResponses = videos != null ? toVideoResponseList(videos) : List.of();

        int total = consulta.getCantidadVideos() != null ? consulta.getCantidadVideos() : 0;
        int disp = consulta.getDisponibles() != null ? consulta.getDisponibles() : 0;
        int noDisp = consulta.getNoDisponibles() != null ? consulta.getNoDisponibles() : 0;

        double pctDisp = total > 0 ? Math.round((disp * 100.0 / total) * 100.0) / 100.0 : 0.0;
        double pctNoDisp = total > 0 ? Math.round((noDisp * 100.0 / total) * 100.0) / 100.0 : 0.0;

        return new ConsultaResponse(
                consulta.getId(),
                playlistId,
                playlistResponse,
                videoResponses,
                consulta.getFechaConsulta(),
                consulta.getCantidadVideos(),
                consulta.getDisponibles(),
                consulta.getNoDisponibles(),
                consulta.getDuracionMs(),
                pctDisp,
                pctNoDisp
        );
    }

    public static List<VideoResponse> toVideoResponseList(
            List<Video> videos) {

        return videos.stream()
                .map(EntityMapper::toVideoResponse)
                .toList();
    }
}
