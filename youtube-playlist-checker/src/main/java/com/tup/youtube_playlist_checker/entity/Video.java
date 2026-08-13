package com.tup.youtube_playlist_checker.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "videos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"playlist_id", "youtube_id"})
})
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @Column(name = "youtube_id", length = 50, nullable = false)
    private String youtubeId;

    @Column(length = 255)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private EstadoVideo estado;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private MotivoIndisponibilidad motivo;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

}