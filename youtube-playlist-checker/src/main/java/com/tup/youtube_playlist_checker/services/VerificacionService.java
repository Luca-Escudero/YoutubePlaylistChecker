package com.tup.youtube_playlist_checker.services;

import com.tup.youtube_playlist_checker.entity.Consulta;
import com.tup.youtube_playlist_checker.entity.Playlist;
import com.tup.youtube_playlist_checker.entity.Video;
import com.tup.youtube_playlist_checker.entity.EstadoVideo;
import com.tup.youtube_playlist_checker.entity.MotivoIndisponibilidad;
import com.tup.youtube_playlist_checker.entity.Usuario;
import com.tup.youtube_playlist_checker.utils.YoutubeUrlParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VerificacionService {

    private final YoutubeService youtubeService;
    private final PlaylistService playlistService;
    private final VideoService videoService;
    private final ConsultaService consultaService;

    public VerificacionService(YoutubeService youtubeService, PlaylistService playlistService, VideoService videoService, ConsultaService consultaService) {
        this.youtubeService = youtubeService;
        this.playlistService = playlistService;
        this.videoService = videoService;
        this.consultaService = consultaService;
    }

    public ResultadoVerificacion verificar(String inputUrlOrId) {
        return verificar(inputUrlOrId, null);
    }

    public ResultadoVerificacion verificar(String inputUrlOrId, Usuario usuario) {
        long inicioMs = System.currentTimeMillis();
        String playlistId = YoutubeUrlParser.extraerPlaylistId(inputUrlOrId);

        if (playlistId == null || playlistId.isBlank()) {
            return null;
        }

        //1. Obtener información de la playlist desde YouTube
        YoutubeService.YoutubePlaylist playlistYoutube = youtubeService.obtenerPlaylist(playlistId);

        if (playlistYoutube == null) {
            return null;
        }

        //2. Crear o actualizar la Playlist
        Playlist playlist = playlistService.obtenerPorYoutubeId(playlistId)
                .orElseGet(() -> {

                    Playlist nuevaPlaylist = new Playlist();

                    nuevaPlaylist.setYoutubeId(playlistYoutube.youtubeId());
                    String tit = (playlistYoutube.titulo() != null && !playlistYoutube.titulo().isBlank())
                            ? playlistYoutube.titulo() : "Playlist " + playlistId;
                    nuevaPlaylist.setTitulo(tit);
                    nuevaPlaylist.setCantidadVideos(playlistYoutube.cantidadVideos());
                    nuevaPlaylist.setUrl("https://www.youtube.com/playlist?list=" + playlistId);
                    nuevaPlaylist.setFechaPrimeraConsulta(java.time.LocalDateTime.now());

                    return playlistService.guardar(nuevaPlaylist);
                });

        if (playlistYoutube.titulo() != null && !playlistYoutube.titulo().isBlank()) {
            playlist.setTitulo(playlistYoutube.titulo());
        }
        playlist.setCantidadVideos(playlistYoutube.cantidadVideos());

        playlist = playlistService.guardar(playlist);

        //3. Obtener videos de la playlist
        List<YoutubeService.YoutubePlaylistVideo> videosYoutube = youtubeService.obtenerVideosDePlaylist(playlistId);

        if (videosYoutube == null) {
            videosYoutube = new ArrayList<>();
        }

        //4. Obtener información detallada de los videos
        List<String> videoIds = videosYoutube.stream()
                .map(YoutubeService.YoutubePlaylistVideo::youtubeId)
                .toList();

        List<YoutubeService.YoutubeVideo> infoVideos = youtubeService.obtenerInformacionVideos(videoIds);

        //5. Determinar estado y actualizar/crear Videos

        int disponibles = 0;
        int noDisponibles = 0;

        List<Video> videosVerificados = new ArrayList<>();

        for (YoutubeService.YoutubePlaylistVideo videoYoutube : videosYoutube) {
            YoutubeService.YoutubeVideo informacion =
                    infoVideos.stream()
                            .filter(video ->
                                    video.youtubeId().equals(videoYoutube.youtubeId())
                            )
                            .findFirst()
                            .orElse(null);

            EstadoVideo estado;
            MotivoIndisponibilidad motivo;
            String titulo;
            String canal;

            if (informacion == null) {
                String titLower = videoYoutube.titulo() != null ? videoYoutube.titulo().toLowerCase() : "";
                String priv = videoYoutube.privacidad();

                if (titLower.contains("private") || titLower.contains("privado") || "private".equalsIgnoreCase(priv)) {
                    estado = EstadoVideo.NO_DISPONIBLE;
                    motivo = MotivoIndisponibilidad.PRIVADO;
                    titulo = (videoYoutube.titulo() != null && !videoYoutube.titulo().isBlank())
                            ? videoYoutube.titulo() : "Video privado";
                } else if (titLower.contains("copyright") || titLower.contains("derechos")) {
                    estado = EstadoVideo.NO_DISPONIBLE;
                    motivo = MotivoIndisponibilidad.COPYRIGHT;
                    titulo = (videoYoutube.titulo() != null && !videoYoutube.titulo().isBlank())
                            ? videoYoutube.titulo() : "Video bloqueado por copyright";
                } else {
                    estado = EstadoVideo.NO_DISPONIBLE;
                    motivo = MotivoIndisponibilidad.ELIMINADO;
                    titulo = (videoYoutube.titulo() != null && !videoYoutube.titulo().isBlank())
                            ? videoYoutube.titulo() : "Video no disponible";
                }
                canal = videoYoutube.canal();

                noDisponibles++;

            } else {

                titulo = informacion.titulo();
                canal = (informacion.canal() != null && !informacion.canal().isBlank())
                        ? informacion.canal() : videoYoutube.canal();

                if ("public".equalsIgnoreCase(informacion.privacidad()) || "unlisted".equalsIgnoreCase(informacion.privacidad())) {

                    estado = EstadoVideo.DISPONIBLE;
                    motivo = null;

                    disponibles++;

                } else {

                    estado = EstadoVideo.NO_DISPONIBLE;
                    motivo = determinarMotivo(informacion);
                    noDisponibles++;
                }
            }

            Video video = videoService.crearOActualizar(
                    playlist,
                    videoYoutube.youtubeId(),
                    titulo,
                    canal,
                    estado,
                    motivo
            );

            videosVerificados.add(video);
        }

        long duracionMs = System.currentTimeMillis() - inicioMs;

        //6. Crear Consulta asociada al usuario
        Consulta consulta = consultaService.registrarConsulta(
                playlist,
                usuario,
                videosYoutube.size(),
                disponibles,
                noDisponibles,
                duracionMs
        );

        //7. Devolver resultado

        return new ResultadoVerificacion(
                playlist,
                videosVerificados,
                consulta,
                videosYoutube.size(),
                disponibles,
                noDisponibles
        );
    }

    //Determinamos el motivo de indisponibilidad según la información devuelta por YouTube

    private MotivoIndisponibilidad determinarMotivo(YoutubeService.YoutubeVideo info) {
        if (info == null) {
            return MotivoIndisponibilidad.DESCONOCIDO;
        }

        if (info.esRestringidoRegional()) {
            return MotivoIndisponibilidad.RESTRICCION_REGIONAL;
        }

        String rej = info.rejectionReason() != null ? info.rejectionReason().toLowerCase() : "";
        if (rej.contains("claim") || rej.contains("copyright") || rej.contains("legal") || rej.contains("termsofuse")) {
            return MotivoIndisponibilidad.COPYRIGHT;
        }

        String priv = info.privacidad() != null ? info.privacidad().toLowerCase() : "";
        if ("private".equalsIgnoreCase(priv)) {
            return MotivoIndisponibilidad.PRIVADO;
        }

        String upStatus = info.uploadStatus() != null ? info.uploadStatus().toLowerCase() : "";
        if ("deleted".equalsIgnoreCase(upStatus) || "failed".equalsIgnoreCase(upStatus) || "rejected".equalsIgnoreCase(upStatus)) {
            return MotivoIndisponibilidad.ELIMINADO;
        }

        return MotivoIndisponibilidad.DESCONOCIDO;
    }

    //Resultado de una verificación completa

    public record ResultadoVerificacion(
            Playlist playlist,
            List<Video> videos,
            Consulta consulta,
            int cantidadVideos,
            int disponibles,
            int noDisponibles
    ) {
    }
}
