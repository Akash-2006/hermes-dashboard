package com.hermes.dashboard.auth;

public record TokenResponse(String token, String username, boolean host) {
}
