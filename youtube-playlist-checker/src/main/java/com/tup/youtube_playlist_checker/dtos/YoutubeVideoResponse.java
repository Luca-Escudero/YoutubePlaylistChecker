package com.tup.youtube_playlist_checker.dtos;

import java.util.List;

public record YoutubeVideoResponse(
        List<Item> items
) {

    public record Item(
            String id,
            Snippet snippet,
            Status status
    ) {
    }

    public record Snippet(
            String title
    ) {
    }

    public record Status(
            String privacyStatus
    ) {
    }
}
