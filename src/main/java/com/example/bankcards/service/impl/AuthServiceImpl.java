package com.example.bankcards.service.impl;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.dto.TokenResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.UserAccount;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserAccountRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwt;

    public AuthServiceImpl(UserAccountRepository users, RoleRepository roles,
                           BCryptPasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Override
    public void register(RegisterRequest req) {
        if (users.existsByUsernameIgnoreCase(req.username()))
            throw new BadRequestException("Username already exists");

        var roleName = req.admin() ? "ADMIN" : "USER";
        var role = roles.findByName(roleName).orElseThrow(
                () -> new BadRequestException("Role " + roleName + " is missing (check Liquibase seed)")
        );

        var user = new UserAccount();
        user.setUsername(req.username());
        user.setPasswordHash(encoder.encode(req.password()));
        user.getRoles().add(role);  // запись создание (попытка фикса)
        users.save(user);
    }

    @Override
    public TokenResponse login(LoginRequest req) {
        var user = users.findByUsernameIgnoreCase(req.username())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        if (!encoder.matches(req.password(), user.getPasswordHash()))
            throw new BadRequestException("Invalid credentials");

        var roleNames = user.getRoles().stream().map(Role::getName).toArray(String[]::new);
        return new TokenResponse(jwt.generate(user.getUsername(), roleNames));
    }
}
