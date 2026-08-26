package com.tup.youtube_playlist_checker.controllers;

import com.tup.youtube_playlist_checker.dtos.ConsultaResponse;
import com.tup.youtube_playlist_checker.dtos.PlaylistResponse;
import com.tup.youtube_playlist_checker.dtos.VideoResponse;
import com.tup.youtube_playlist_checker.mapper.EntityMapper;
import com.tup.youtube_playlist_checker.services.VerificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/verificaciones")
public class VerificacionController {

    private final VerificacionService verificacionService;

    public VerificacionController(
            VerificacionService verificacionService) {

        this.verificacionService = verificacionService;
    }

    @PostMapping("/{playlistId}")
    public ResponseEntity<ResultadoVerificacionResponse> verificar(@PathVariable String playlistId) {

        VerificacionService.ResultadoVerificacion resultado = verificacionService.verificar(playlistId);

        if (resultado == null) {
            return ResponseEntity.notFound().build();
        }

        ResultadoVerificacionResponse response = new ResultadoVerificacionResponse(
                        EntityMapper.toPlaylistResponse(resultado.playlist()),
                        EntityMapper.toVideoResponseList(resultado.videos()),
                        EntityMapper.toConsultaResponse(resultado.consulta()),
                        resultado.cantidadVideos(),
                        resultado.disponibles(),
                        resultado.noDisponibles()
                );

        return ResponseEntity.ok(response);
    }

    public record ResultadoVerificacionResponse(
            PlaylistResponse playlist,
            List<VideoResponse> videos,
            ConsultaResponse consulta,
            int cantidadVideos,
            int disponibles,
            int noDisponibles
    ) {
    }
}
