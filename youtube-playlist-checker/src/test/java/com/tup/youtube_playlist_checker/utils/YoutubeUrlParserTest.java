package com.tup.youtube_playlist_checker.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class YoutubeUrlParserTest {

    @Test
    void testExtraerPlaylistIdConUrlCompleta() {
        String url = "https://www.youtube.com/playlist?list=PL1234567890ABCDEF";
        String id = YoutubeUrlParser.extraerPlaylistId(url);
        assertEquals("PL1234567890ABCDEF", id);
    }

    @Test
    void testExtraerPlaylistIdConIdDirecto() {
        String input = "PL1234567890ABCDEF";
        String id = YoutubeUrlParser.extraerPlaylistId(input);
        assertEquals("PL1234567890ABCDEF", id);
    }

    @Test
    void testExtraerPlaylistIdConUrlNulaOVacia() {
        assertNull(YoutubeUrlParser.extraerPlaylistId(null));
        assertNull(YoutubeUrlParser.extraerPlaylistId("   "));
    }
}
