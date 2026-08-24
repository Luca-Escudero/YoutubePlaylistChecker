package com.tup.youtube_playlist_checker.controllers;

import com.tup.youtube_playlist_checker.entity.Playlist;
import com.tup.youtube_playlist_checker.services.PlaylistService;
import com.tup.youtube_playlist_checker.services.YoutubeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final YoutubeService youtubeService;

    public PlaylistController(PlaylistService playlistService, YoutubeService youtubeService) {
        this.playlistService = playlistService;
        this.youtubeService = youtubeService;
    }

    @GetMapping("/youtube/test/{youtubeId}")
    public ResponseEntity<String> obtenerDesdeYoutube(@PathVariable String youtubeId) {
        return ResponseEntity.ok(youtubeService.obtenerPlaylist(youtubeId));
    }

    @GetMapping
    public ResponseEntity<List<Playlist>> obtenerTodas() {
        return ResponseEntity.ok(playlistService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Playlist> obtenerPorId(@PathVariable Long id) {
        Optional<Playlist> playlist = playlistService.obtenerPorId(id);

        if (playlist.isPresent()) {
            return ResponseEntity.ok(playlist.get());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/youtube/{youtubeId}")
    public ResponseEntity<Playlist> obtenerPorYoutubeId(@PathVariable String youtubeId) {
        Optional<Playlist> playlist = playlistService.obtenerPorYoutubeId(youtubeId);

        if (playlist.isPresent()) {
            return ResponseEntity.ok(playlist.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Playlist> guardar(@RequestBody Playlist playlist) {
        Playlist playlistGuardada = playlistService.guardar(playlist);
        return ResponseEntity.ok(playlistGuardada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        playlistService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
