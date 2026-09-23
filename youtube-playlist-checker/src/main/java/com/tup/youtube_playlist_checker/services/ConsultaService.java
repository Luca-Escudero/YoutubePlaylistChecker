package com.tup.youtube_playlist_checker.services;

import com.tup.youtube_playlist_checker.entity.Consulta;
import com.tup.youtube_playlist_checker.entity.Playlist;
import com.tup.youtube_playlist_checker.repositories.ConsultaRepository;
import org.springframework.stereotype.Service;

import com.tup.youtube_playlist_checker.entity.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;

    public ConsultaService(ConsultaRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    public List<Consulta> obtenerTodas() {
        return consultaRepository.findAll();
    }

    public Optional<Consulta> obtenerPorId(Long id) {
        return consultaRepository.findById(id);
    }

    public List<Consulta> obtenerPorPlaylist(Playlist playlist) {
        return consultaRepository.findByPlaylist(playlist);
    }

    public List<Consulta> obtenerPorUsuario(Usuario usuario) {
        return consultaRepository.findByUsuario(usuario);
    }

    public List<Consulta> obtenerPorUsuarioId(Long usuarioId) {
        return consultaRepository.findByUsuarioIdOrderByFechaConsultaDesc(usuarioId);
    }

    public Consulta registrarConsulta(
            Playlist playlist,
            Usuario usuario,
            int cantidadVideos,
            int disponibles,
            int noDisponibles,
            Long duracionMs) {

        Consulta consulta = new Consulta();

        consulta.setPlaylist(playlist);
        consulta.setUsuario(usuario);
        consulta.setFechaConsulta(LocalDateTime.now());
        consulta.setCantidadVideos(cantidadVideos);
        consulta.setDisponibles(disponibles);
        consulta.setNoDisponibles(noDisponibles);
        consulta.setDuracionMs(duracionMs);

        return consultaRepository.save(consulta);
    }

    public Consulta registrarConsulta(
            Playlist playlist,
            int cantidadVideos,
            int disponibles,
            int noDisponibles,
            Long duracionMs) {
        return registrarConsulta(playlist, null, cantidadVideos, disponibles, noDisponibles, duracionMs);
    }

    public Consulta guardar(Consulta consulta) {
        return consultaRepository.save(consulta);
    }

    public void eliminar(Long id) {
        consultaRepository.deleteById(id);
    }
}
