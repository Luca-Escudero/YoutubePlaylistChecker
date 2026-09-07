package com.tup.youtube_playlist_checker.controllers;

import com.tup.youtube_playlist_checker.dtos.ConsultaResponse;
import com.tup.youtube_playlist_checker.dtos.PlaylistResponse;
import com.tup.youtube_playlist_checker.dtos.VideoResponse;
import com.tup.youtube_playlist_checker.entity.Usuario;
import com.tup.youtube_playlist_checker.mapper.EntityMapper;
import com.tup.youtube_playlist_checker.repositories.UsuarioRepository;
import com.tup.youtube_playlist_checker.services.VerificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/verificaciones")
public class VerificacionController {

    private final VerificacionService verificacionService;
    private final UsuarioRepository usuarioRepository;

    public VerificacionController(VerificacionService verificacionService, UsuarioRepository usuarioRepository) {
        this.verificacionService = verificacionService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<ResultadoVerificacionResponse> verificarUrl(@RequestBody VerificationRequest request) {
        if (request == null || request.url() == null || request.url().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ejecutarVerificacion(request.url());
    }

    @PostMapping("/{playlistIdOrUrl}")
    public ResponseEntity<ResultadoVerificacionResponse> verificarPathVariable(@PathVariable String playlistIdOrUrl) {
        return ejecutarVerificacion(playlistIdOrUrl);
    }

    private ResponseEntity<ResultadoVerificacionResponse> ejecutarVerificacion(String inputUrlOrId) {
        Usuario usuarioLogueado = getAuthenticatedUsuario();

        VerificacionService.ResultadoVerificacion resultado = verificacionService.verificar(inputUrlOrId, usuarioLogueado);

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

    private Usuario getAuthenticatedUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails userDetails) {
            return usuarioRepository.findByEmail(userDetails.getUsername()).orElse(null);
        }
        return null;
    }

    public record VerificationRequest(String url) {}

    public record ResultadoVerificacionResponse(
            PlaylistResponse playlist,
            List<VideoResponse> videos,
            ConsultaResponse consulta,
            int cantidadVideos,
            int disponibles,
            int noDisponibles
    ) {}
}
