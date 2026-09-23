package com.tup.youtube_playlist_checker.dtos;

import java.time.LocalDateTime;

import java.util.List;

public record ConsultaResponse(
        Long id,
        Long playlistId,
        PlaylistResponse playlist,
        List<VideoResponse> videos,
        LocalDateTime fechaConsulta,
        Integer cantidadVideos,
        Integer disponibles,
        Integer noDisponibles,
        Long duracionMs,
        Double porcentajeDisponibles,
        Double porcentajeNoDisponibles
) {
}
