package com.example.bankcards.service;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.dto.TokenResponse;

public interface AuthService {
    void register(RegisterRequest req);
    TokenResponse login(LoginRequest req);
}
