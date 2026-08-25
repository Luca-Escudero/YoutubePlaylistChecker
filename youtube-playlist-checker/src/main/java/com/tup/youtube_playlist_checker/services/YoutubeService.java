package com.tup.youtube_playlist_checker.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tup.youtube_playlist_checker.dtos.YoutubePlaylistItemResponse;
import com.tup.youtube_playlist_checker.dtos.YoutubePlaylistResponse;
import com.tup.youtube_playlist_checker.dtos.YoutubeVideoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class YoutubeService {

    private static final String YOUTUBE_API_URL =
            "https://www.googleapis.com/youtube/v3";

    private final RestClient restClient;
    private final String apiKey;

    public YoutubeService(
            RestClient.Builder restClientBuilder,
            @Value("${youtube.api.key}")
            String apiKey) {

        this.restClient = restClientBuilder
                .baseUrl(YOUTUBE_API_URL)
                .build();

        this.apiKey = apiKey;
    }

    //Traemos información básica de una playlist de YouTube

    public YoutubePlaylist obtenerPlaylist(String playlistId) {

        YoutubePlaylistResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/playlists")
                        .queryParam("part", "snippet,contentDetails")
                        .queryParam("id", playlistId)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .body(YoutubePlaylistResponse.class);

        if (response == null || response.items() == null || response.items().isEmpty()) {
            return null;
        }

        YoutubePlaylistItemResponse item = response.items().getFirst();

        String titulo = null;
        int cantidadVideos = 0;

        if (item.snippet() != null) {
            titulo = item.snippet().title();
        }

        if (item.contentDetails() != null && item.contentDetails().itemCount() != null) {

            cantidadVideos = item.contentDetails().itemCount();
        }

        return new YoutubePlaylist(
                item.id(),
                titulo,
                cantidadVideos
        );
    }

    //Conseguimos todos los videos de una playlist
    //Se maneja la paginación de la API para obtener todos los elementos

    public List<YoutubePlaylistVideo> obtenerVideosDePlaylist(String playlistId) {

        List<YoutubePlaylistVideo> videos = new ArrayList<>();

        String nextPageToken = null;

        do {
            final String pageToken = nextPageToken;

            YoutubePlaylistResponse response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/playlistItems")
                                .queryParam("part", "snippet,contentDetails")
                                .queryParam("playlistId", playlistId)
                                .queryParam("maxResults", 50)
                                .queryParam("key", apiKey);

                        if (pageToken != null) {
                            builder.queryParam("pageToken", pageToken);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .body(YoutubePlaylistResponse.class);

            if (response == null || response.items() == null) {
                break;
            }

            for (YoutubePlaylistItemResponse item : response.items()) {

                String videoId = obtenerVideoId(item);

                if (videoId == null) {
                    continue;
                }

                String titulo = null;

                if (item.snippet() != null) {
                    titulo = item.snippet().title();
                }

                videos.add(new YoutubePlaylistVideo(
                        videoId,
                        titulo
                ));
            }


            nextPageToken = response.nextPageToken();

        } while (nextPageToken != null && !nextPageToken.isBlank());

        return videos;
    }

    //Obtiene información de los videos de YouTube
    public List<YoutubeVideo> obtenerInformacionVideos(List<String> videoIds) {

        if (videoIds == null || videoIds.isEmpty()) {
            return List.of();
        }

        List<YoutubeVideo> videos = new ArrayList<>();

        /*
         * La API permite consultar hasta 50 videos por petición.
         */
        for (int inicio = 0; inicio < videoIds.size(); inicio += 50) {

            int fin = Math.min(inicio + 50, videoIds.size());

            List<String> lote = videoIds.subList(inicio, fin);

            YoutubeVideoResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/videos")
                            .queryParam("part", "snippet,status,contentDetails")
                            .queryParam("id", String.join(",", lote))
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(YoutubeVideoResponse.class);

            if (response == null || response.items() == null) {
                continue;
            }

            for (YoutubeVideoResponse.Item item : response.items()) {

                String titulo = null;
                String privacidad = null;

                if (item.snippet() != null) {
                    titulo = item.snippet().title();
                }

                if (item.status() != null) {
                    privacidad = item.status().privacyStatus();
                }

                videos.add(new YoutubeVideo(
                        item.id(),
                        titulo,
                        privacidad
                ));
            }
        }

        return videos;
    }

    private String obtenerVideoId(YoutubePlaylistItemResponse item) {

        if (item.snippet() != null &&
                item.snippet().resourceId() != null) {

            return item.snippet()
                    .resourceId()
                    .videoId();
        }

        if (item.contentDetails() != null) {
            return item.contentDetails().videoId();
        }

        return null;
    }

    // Objetos que utilizamos dentro de nuestro archivo

    public record YoutubePlaylist(
            String youtubeId,
            String titulo,
            int cantidadVideos
    ) {
    }

    public record YoutubePlaylistVideo(
            String youtubeId,
            String titulo
    ) {
    }

    public record YoutubeVideo(
            String youtubeId,
            String titulo,
            String privacidad
    ) {
    }
}