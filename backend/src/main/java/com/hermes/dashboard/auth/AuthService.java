package com.hermes.dashboard.auth;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AccountService accounts;
    private final JwtService jwtService;

    public AuthService(AccountService accounts, JwtService jwtService) {
        this.accounts = accounts;
        this.jwtService = jwtService;
    }

    public String login(String username, String password) {
        return accounts.login(username, password, jwtService);
    }

    public boolean isHost(Authentication authentication) {
        return authentication != null && accounts.isHost(authentication.getName());
    }
}
