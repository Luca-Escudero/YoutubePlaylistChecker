package com.tup.youtube_playlist_checker.dtos;

public record YoutubePlaylistItemResponse(
        String id,
        Snippet snippet,
        ContentDetails contentDetails
) {

    public record Snippet(
            String title,
            ResourceId resourceId
    ) {
    }

    public record ResourceId(
            String videoId
    ) {
    }

    public record ContentDetails(
            Integer itemCount,
            String videoId
    ) {
    }
}