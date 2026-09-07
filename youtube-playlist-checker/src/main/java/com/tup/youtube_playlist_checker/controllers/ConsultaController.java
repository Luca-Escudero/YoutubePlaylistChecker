package com.tup.youtube_playlist_checker.controllers;

import com.tup.youtube_playlist_checker.dtos.ConsultaResponse;
import com.tup.youtube_playlist_checker.entity.Consulta;
import com.tup.youtube_playlist_checker.entity.Usuario;
import com.tup.youtube_playlist_checker.mapper.EntityMapper;
import com.tup.youtube_playlist_checker.repositories.UsuarioRepository;
import com.tup.youtube_playlist_checker.services.ConsultaService;
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

    public ConsultaController(ConsultaService consultaService, UsuarioRepository usuarioRepository) {
        this.consultaService = consultaService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/mis-consultas")
    public ResponseEntity<List<ConsultaResponse>> obtenerMisConsultas() {
        Usuario usuario = getAuthenticatedUsuario();
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        List<Consulta> consultas = consultaService.obtenerPorUsuarioId(usuario.getId());
        List<ConsultaResponse> response = consultas.stream()
                .map(EntityMapper::toConsultaResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponse> obtenerPorId(@PathVariable Long id) {
        Optional<Consulta> consulta = consultaService.obtenerPorId(id);
        return consulta.map(c -> ResponseEntity.ok(EntityMapper.toConsultaResponse(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Usuario getAuthenticatedUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails userDetails) {
            return usuarioRepository.findByEmail(userDetails.getUsername()).orElse(null);
        }
        return null;
    }
}
