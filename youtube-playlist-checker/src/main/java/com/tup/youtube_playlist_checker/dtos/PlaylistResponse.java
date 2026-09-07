package com.tup.youtube_playlist_checker.dtos;

import java.time.LocalDateTime;

public record PlaylistResponse(
        Long id,
        String youtubeId,
        String url,
        String titulo,
        Integer cantidadVideos,
        LocalDateTime fechaPrimeraConsulta
) {
}
