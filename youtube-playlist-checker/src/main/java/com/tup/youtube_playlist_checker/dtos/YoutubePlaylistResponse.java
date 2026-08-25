package com.tup.youtube_playlist_checker.dtos;

import java.util.List;

public record YoutubePlaylistResponse(
        String nextPageToken,
        List<YoutubePlaylistItemResponse> items
) {
}
