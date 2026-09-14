package com.tup.youtube_playlist_checker.dtos.auth;

public record AuthResponse(
        String token,
        String type,
        String email,
        String nombre
) {
    public AuthResponse(String token, String email, String nombre) {
        this(token, "Bearer", email, nombre);
    }
}
