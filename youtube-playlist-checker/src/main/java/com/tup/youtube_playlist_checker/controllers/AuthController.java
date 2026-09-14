package com.tup.youtube_playlist_checker.controllers;

import com.tup.youtube_playlist_checker.dtos.auth.AuthResponse;
import com.tup.youtube_playlist_checker.dtos.auth.LoginRequest;
import com.tup.youtube_playlist_checker.dtos.auth.RegisterRequest;
import com.tup.youtube_playlist_checker.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerAlias(@Valid @RequestBody RegisterRequest request) {
        return registrar(request);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
