package com.tup.youtube_playlist_checker.dtos;

import com.tup.youtube_playlist_checker.entity.EstadoVideo;
import com.tup.youtube_playlist_checker.entity.MotivoIndisponibilidad;

import java.time.LocalDateTime;

public record VideoResponse(
        Long id,
        String youtubeId,
        String titulo,
        EstadoVideo estado,
        MotivoIndisponibilidad motivo,
        LocalDateTime ultimaActualizacion
) {
}