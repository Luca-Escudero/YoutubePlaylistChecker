package com.tup.youtube_playlist_checker.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class YoutubeService {

    private static final String YOUTUBE_API_URL =
            "https://www.googleapis.com/youtube/v3";

    private final RestClient restClient;

    @Value("${youtube.api.key}")
    private String apiKey;

    public YoutubeService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(YOUTUBE_API_URL)
                .build();
    }

    public String obtenerPlaylist(String playlistId) {

        ResponseEntity<String> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/playlists")
                        .queryParam("part", "snippet,contentDetails")
                        .queryParam("id", playlistId)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .toEntity(String.class);

        return response.getBody();
    }
}
