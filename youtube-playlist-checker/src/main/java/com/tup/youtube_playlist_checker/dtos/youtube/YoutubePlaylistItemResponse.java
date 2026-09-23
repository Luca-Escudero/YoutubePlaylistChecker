package com.tup.youtube_playlist_checker.dtos.youtube;

public record YoutubePlaylistItemResponse(
        String id,
        Snippet snippet,
        Status status,
        ContentDetails contentDetails
) {

    public record Snippet(
            String title,
            String videoOwnerChannelTitle,
            ResourceId resourceId
    ) {
    }

    public record Status(
            String privacyStatus
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