package com.tup.youtube_playlist_checker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime; // No te olvides de importar esto para la fecha
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "youtube_id", length = 50, nullable = false, unique = true)
    private String youtubeId;

    @Column(length = 255, nullable = false)
    private String url;

    @Column(length = 255)
    private String titulo;

    @Column(name = "cantidad_videos")
    private Integer cantidadVideos;

    @Column(name = "fecha_primera_consulta")
    private LocalDateTime fechaPrimeraConsulta;

}