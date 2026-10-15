package com.hermes.dashboard.auth;

public record RegisterRequest(String username, String password) {
    public boolean isComplete() {
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }
}
