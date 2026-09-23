package com.tup.youtube_playlist_checker.dtos.youtube;

import java.util.List;

public record YoutubeVideoResponse(
        List<Item> items
) {

    public record Item(
            String id,
            Snippet snippet,
            Status status,
            ContentDetails contentDetails
    ) {
    }

    public record Snippet(
            String title,
            String channelTitle
    ) {
    }

    public record Status(
            String privacyStatus,
            String uploadStatus,
            String rejectionReason
    ) {
    }

    public record ContentDetails(
            RegionRestriction regionRestriction
    ) {
    }

    public record RegionRestriction(
            List<String> allowed,
            List<String> blocked
    ) {
    }
}
