package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.dto.TokenResponse;
import com.example.bankcards.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public void register(@RequestBody @Valid RegisterRequest req) { auth.register(req); }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest req) { return auth.login(req); }
}
