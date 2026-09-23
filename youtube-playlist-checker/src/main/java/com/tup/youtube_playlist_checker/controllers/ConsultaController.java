package com.tup.youtube_playlist_checker.controllers;

import com.tup.youtube_playlist_checker.dtos.ConsultaResponse;
import com.tup.youtube_playlist_checker.entity.Consulta;
import com.tup.youtube_playlist_checker.entity.Usuario;
import com.tup.youtube_playlist_checker.mapper.EntityMapper;
import com.tup.youtube_playlist_checker.repositories.UsuarioRepository;
import com.tup.youtube_playlist_checker.services.ConsultaService;
import com.tup.youtube_playlist_checker.services.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;
    private final UsuarioRepository usuarioRepository;
    private final VideoService videoService;

    public ConsultaController(ConsultaService consultaService, UsuarioRepository usuarioRepository, VideoService videoService) {
        this.consultaService = consultaService;
        this.usuarioRepository = usuarioRepository;
        this.videoService = videoService;
    }

    @GetMapping("/mis-consultas")
    public ResponseEntity<List<ConsultaResponse>> obtenerMisConsultas() {
        Usuario usuario = getAuthenticatedUsuario();
        List<Consulta> consultas;

        if (usuario != null) {
            consultas = consultaService.obtenerPorUsuarioId(usuario.getId());
            if (consultas == null || consultas.isEmpty()) {
                consultas = consultaService.obtenerTodas();
            }
        } else {
            consultas = consultaService.obtenerTodas();
        }

        List<ConsultaResponse> response = consultas.stream()
                .map(EntityMapper::toConsultaResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponse> obtenerPorId(@PathVariable Long id) {
        Optional<Consulta> consulta = consultaService.obtenerPorId(id);
        return consulta.map(c -> {
            var videos = videoService.obtenerPorPlaylist(c.getPlaylist());
            return ResponseEntity.ok(EntityMapper.toConsultaResponse(c, videos));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/playlist/{playlistId}")
    public ResponseEntity<List<ConsultaResponse>> obtenerPorPlaylist(@PathVariable Long playlistId) {
        var playlist = new com.tup.youtube_playlist_checker.entity.Playlist();
        playlist.setId(playlistId);

        List<Consulta> consultas = consultaService.obtenerPorPlaylist(playlist);
        List<ConsultaResponse> response = consultas.stream()
                .map(EntityMapper::toConsultaResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/comparar")
    public ResponseEntity<com.tup.youtube_playlist_checker.dtos.ComparacionConsultaResponse> comparar(
            @RequestParam Long idAnterior,
            @RequestParam Long idNueva) {

        Optional<Consulta> optAnterior = consultaService.obtenerPorId(idAnterior);
        Optional<Consulta> optNueva = consultaService.obtenerPorId(idNueva);

        if (optAnterior.isEmpty() || optNueva.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Consulta anterior = optAnterior.get();
        Consulta nueva = optNueva.get();

        var videos = videoService.obtenerPorPlaylist(nueva.getPlaylist());

        ConsultaResponse respAnterior = EntityMapper.toConsultaResponse(anterior, videos);
        ConsultaResponse respNueva = EntityMapper.toConsultaResponse(nueva, videos);

        var videosNoDisponibles = videos.stream()
                .filter(v -> v.getEstado() == com.tup.youtube_playlist_checker.entity.EstadoVideo.NO_DISPONIBLE)
                .map(EntityMapper::toVideoResponse)
                .toList();

        var videosDisponibles = videos.stream()
                .filter(v -> v.getEstado() == com.tup.youtube_playlist_checker.entity.EstadoVideo.DISPONIBLE)
                .map(EntityMapper::toVideoResponse)
                .toList();

        int diffDisp = (nueva.getDisponibles() != null ? nueva.getDisponibles() : 0)
                - (anterior.getDisponibles() != null ? anterior.getDisponibles() : 0);

        int diffNoDisp = (nueva.getNoDisponibles() != null ? nueva.getNoDisponibles() : 0)
                - (anterior.getNoDisponibles() != null ? anterior.getNoDisponibles() : 0);

        var response = new com.tup.youtube_playlist_checker.dtos.ComparacionConsultaResponse(
                respAnterior,
                respNueva,
                videosNoDisponibles,
                videosDisponibles,
                List.of(),
                diffDisp,
                diffNoDisp
        );

        return ResponseEntity.ok(response);
    }

    private Usuario getAuthenticatedUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            String email = null;
            if (principal instanceof UserDetails userDetails) {
                email = userDetails.getUsername();
            } else if (principal instanceof String str && !"anonymousUser".equals(str)) {
                email = str;
            }
            if (email != null) {
                return usuarioRepository.findByEmail(email).orElse(null);
            }
        }
        return null;
    }
}
