package com.example.bankcards.security;

import com.example.bankcards.repository.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserAccountRepository users;

    public JwtAuthFilter(JwtService jwt, UserAccountRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        var auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            var token = auth.substring(7);
            try {
                var username = jwt.getUsername(token);
                var roles = Arrays.stream(jwt.getRoles(token))
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .toList();

                var userOpt = users.findByUsernameIgnoreCase(username);
                if (userOpt.isPresent() && userOpt.get().isEnabled()) {
                    var userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(userOpt.get().getUsername())
                            .password(userOpt.get().getPasswordHash())
                            .authorities(roles)
                            .build();
                    var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, roles);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ignored) { /* токен плохой — идём дальше без контекста */ }
        }
        chain.doFilter(req, res);
    }
}
