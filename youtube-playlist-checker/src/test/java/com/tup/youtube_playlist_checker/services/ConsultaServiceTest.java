package com.tup.youtube_playlist_checker.services;

import com.tup.youtube_playlist_checker.entity.Consulta;
import com.tup.youtube_playlist_checker.entity.Playlist;
import com.tup.youtube_playlist_checker.repositories.ConsultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private ConsultaService consultaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegistrarConsultaConDuracion() {
        Playlist playlist = new Playlist();
        playlist.setId(1L);
        playlist.setYoutubeId("PL123");

        Consulta mockConsulta = new Consulta();
        mockConsulta.setId(10L);
        mockConsulta.setPlaylist(playlist);
        mockConsulta.setCantidadVideos(10);
        mockConsulta.setDisponibles(8);
        mockConsulta.setNoDisponibles(2);
        mockConsulta.setDuracionMs(350L);

        when(consultaRepository.save(any(Consulta.class))).thenReturn(mockConsulta);

        Consulta resultado = consultaService.registrarConsulta(playlist, 10, 8, 2, 350L);

        assertNotNull(resultado);
        assertEquals(350L, resultado.getDuracionMs());
        assertEquals(8, resultado.getDisponibles());
        assertEquals(2, resultado.getNoDisponibles());
        verify(consultaRepository, times(1)).save(any(Consulta.class));
    }
}
