package com.hermes.dashboard.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AccountService accounts;

    public AuthController(AuthService authService, AccountService accounts) {
        this.authService = authService;
        this.accounts = accounts;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (!request.isComplete()) {
            return ResponseEntity.badRequest().body("username and password are required");
        }
        String token = authService.login(request.username(), request.password());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid credentials");
        }
        return ResponseEntity.ok(new TokenResponse(token, request.username(), accounts.isHost(request.username())));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (!request.isComplete()) {
            return ResponseEntity.badRequest().body("username and password are required");
        }
        if (accounts.register(request.username().trim(), request.password()) == AccountService.RegisterResult.ALREADY_EXISTS) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("username already taken");
        }
        return ResponseEntity.ok("account created — waiting for host approval");
    }

    @GetMapping("/pending")
    public ResponseEntity<?> pending(Authentication authentication) {
        if (!authService.isHost(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "host only"));
        }
        return ResponseEntity.ok(accounts.pending().stream()
                .map(a -> Map.of("username", a.getUsername()))
                .toList());
    }

    @PostMapping("/decide")
    public ResponseEntity<?> decide(@RequestBody Map<String, Object> body, Authentication authentication) {
        if (!authService.isHost(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "host only"));
        }
        String username = body.get("username") == null ? "" : body.get("username").toString();
        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        boolean ok = accounts.decide(username, approve);
        return ok
                ? ResponseEntity.ok(Map.of("status", approve ? "approved" : "rejected"))
                : ResponseEntity.badRequest().body(Map.of("error", "no pending account with that username"));
    }
}
