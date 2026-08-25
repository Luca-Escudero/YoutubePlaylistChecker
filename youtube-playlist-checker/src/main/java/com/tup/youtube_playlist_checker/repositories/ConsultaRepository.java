package com.tup.youtube_playlist_checker.repositories;

import com.tup.youtube_playlist_checker.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository <Consulta, Long>{

    List<Consulta> findByPlaylistIdOrderByFechaConsultaDesc(Long playlistId);
}
