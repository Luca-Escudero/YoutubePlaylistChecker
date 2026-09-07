package com.tup.youtube_playlist_checker.services;

import com.tup.youtube_playlist_checker.entity.Consulta;
import com.tup.youtube_playlist_checker.entity.Playlist;
import com.tup.youtube_playlist_checker.entity.Video;
import com.tup.youtube_playlist_checker.entity.EstadoVideo;
import com.tup.youtube_playlist_checker.entity.MotivoIndisponibilidad;
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

    //Ejecuta una verificación completa de una playlist.

    public ResultadoVerificacion verificar(String playlistId) {
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
                    nuevaPlaylist.setTitulo(playlistYoutube.titulo());
                    nuevaPlaylist.setCantidadVideos(playlistYoutube.cantidadVideos());
                    nuevaPlaylist.setUrl("https://www.youtube.com/playlist?list=" + playlistId);

                    return playlistService.guardar(nuevaPlaylist);
                });

        playlist.setTitulo(playlistYoutube.titulo());
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

            if (informacion == null) {

                estado = EstadoVideo.NO_DISPONIBLE;
                motivo = MotivoIndisponibilidad.ELIMINADO;
                titulo = videoYoutube.titulo();

                noDisponibles++;

            } else {

                titulo = informacion.titulo();

                if ("public".equalsIgnoreCase(informacion.privacidad())) {

                    estado = EstadoVideo.DISPONIBLE;
                    motivo = null;

                    disponibles++;

                } else {

                    estado = EstadoVideo.NO_DISPONIBLE;

                    motivo = determinarMotivo(informacion.privacidad()
                    );

                    noDisponibles++;
                }
            }

            Video video = videoService.crearOActualizar(
                    playlist,
                    videoYoutube.youtubeId(),
                    titulo,
                    estado,
                    motivo
            );

            videosVerificados.add(video);
        }

        //6. Crear Consulta

        Consulta consulta = consultaService.registrarConsulta(
                playlist,
                videosYoutube.size(),
                disponibles,
                noDisponibles
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

    //Determinamos el motivo de indisponibilidad según el privacyStatus devuelto por YouTube

    private MotivoIndisponibilidad determinarMotivo(String privacidad) {

        if (privacidad == null) {
            return MotivoIndisponibilidad.DESCONOCIDO;
        }

        return switch (privacidad.toLowerCase()) {

            case "private" ->
                    MotivoIndisponibilidad.PRIVADO;

            default ->
                    MotivoIndisponibilidad.DESCONOCIDO;
        };
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
