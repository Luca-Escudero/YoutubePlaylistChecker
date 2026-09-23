package com.tup.youtube_playlist_checker.dtos;

import java.util.List;

public record ComparacionConsultaResponse(
        ConsultaResponse consultaAnterior,
        ConsultaResponse consultaNueva,
        List<VideoResponse> videosAhoraNoDisponibles,
        List<VideoResponse> videosRecuperados,
        List<VideoResponse> videosSinCambio,
        int diferenciaDisponibles,
        int diferenciaNoDisponibles
) {
}
