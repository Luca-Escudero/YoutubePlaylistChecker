package com.tup.youtube_playlist_checker.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

        YoutubePlaylistItem item = response.items().getFirst();

        return new YoutubePlaylist(
                item.id(),
                item.snippet().title(),
                item.contentDetails().itemCount()
        );
    }

    //Conseguimos todos los videos de una playlist
    //Se maneja la paginación de la API para obtener todos los elementos

    public List<YoutubePlaylistVideo> obtenerVideosDePlaylist(String playlistId) {

        List<YoutubePlaylistVideo> videos = new ArrayList<>();

        String nextPageToken = null;

        do {
            final String pageToken = nextPageToken;

            YoutubePlaylistItemsResponse response = restClient.get()
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
                    .body(YoutubePlaylistItemsResponse.class);

            if (response == null || response.items() == null) {
                break;
            }

            for (YoutubePlaylistItemVideo item : response.items()) {

                String videoId = null;

                if (item.contentDetails() != null) {
                    videoId = item.contentDetails().videoId();
                }

                if (videoId == null && item.snippet() != null
                        && item.snippet().resourceId() != null) {
                    videoId = item.snippet().resourceId().videoId();
                }

                if (videoId != null) {
                    String title = item.snippet() != null
                            ? item.snippet().title()
                            : null;

                    videos.add(new YoutubePlaylistVideo(
                            videoId,
                            title
                    ));
                }
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

            YoutubeVideosResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/videos")
                            .queryParam("part", "snippet,status,contentDetails")
                            .queryParam("id", String.join(",", lote))
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(YoutubeVideosResponse.class);

            if (response == null || response.items() == null) {
                continue;
            }

            for (YoutubeVideoItem item : response.items()) {

                String titulo = item.snippet() != null
                        ? item.snippet().title()
                        : null;

                String privacidad = item.status() != null
                        ? item.status().privacyStatus()
                        : null;

                videos.add(new YoutubeVideo(
                        item.id(),
                        titulo,
                        privacidad
                ));
            }
        }

        return videos;
    }


    // Objetos que utilizamos dentro de nuestra aplicación

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

    // Respuestas de la API de YouTube

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YoutubePlaylistResponse(
            List<YoutubePlaylistItem> items
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YoutubePlaylistItem(
            String id,
            YoutubeSnippet snippet,
            YoutubeContentDetails contentDetails
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YoutubePlaylistItemsResponse(
            String nextPageToken,
            List<YoutubePlaylistItemVideo> items
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YoutubePlaylistItemVideo(
            YoutubeSnippet snippet,
            YoutubePlaylistItemContentDetails contentDetails
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YoutubePlaylistItemContentDetails(
            String videoId
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YoutubeVideosResponse(
            List<YoutubeVideoItem> items
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YoutubeVideoItem(
            String id,
            YoutubeSnippet snippet,
            YoutubeVideoStatus status
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YoutubeSnippet(
            String title,
            YoutubeResourceId resourceId
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YoutubeResourceId(
            String videoId
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YoutubeContentDetails(
            int itemCount
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YoutubeVideoStatus(
            @JsonProperty("privacyStatus")
            String privacyStatus
    ) {
    }
}