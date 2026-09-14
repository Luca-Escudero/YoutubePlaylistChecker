package com.tup.youtube_playlist_checker.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeUrlParser {

    private static final Pattern PLAYLIST_ID_PATTERN = Pattern.compile("[?&]list=([a-zA-Z0-9_-]+)");

    /**
     * Extrae el playlistId de una URL completa de YouTube o devuelve la cadena limpia si ya es un ID directo.
     */
    public static String extraerPlaylistId(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String trimmed = input.trim();

        Matcher matcher = PLAYLIST_ID_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // Si no es una URL con protocolo o slashes, asumimos que es un ID directo
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://") && !trimmed.contains("/")) {
            return trimmed;
        }

        return trimmed;
    }
}
