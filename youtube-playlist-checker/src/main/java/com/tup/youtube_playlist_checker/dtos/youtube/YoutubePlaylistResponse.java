package com.tup.youtube_playlist_checker.dtos.youtube;

import java.util.List;

public record YoutubePlaylistResponse(
        String nextPageToken,
        List<YoutubePlaylistItemResponse> items
) {
}
