package com.hermes.dashboard.auth;

public record LoginRequest(String username, String password) {
    public boolean isComplete() {
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }
}
