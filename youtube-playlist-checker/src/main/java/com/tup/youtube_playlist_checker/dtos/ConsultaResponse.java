package com.tup.youtube_playlist_checker.dtos;

import java.time.LocalDateTime;

public record ConsultaResponse(
        Long id,
        Long playlistId,
        LocalDateTime fechaConsulta,
        Integer cantidadVideos,
        Integer disponibles,
        Integer noDisponibles
) {
}
