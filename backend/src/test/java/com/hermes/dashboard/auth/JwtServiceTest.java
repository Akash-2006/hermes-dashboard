package com.hermes.dashboard.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-long-enough-for-hs256!!";

    @Test
    void issuesAndValidatesToken() {
        JwtService service = new JwtService(SECRET, 10);
        String token = service.issueToken("admin");
        assertEquals("admin", service.validateToken(token).orElseThrow());
    }

    @Test
    void rejectsTamperedToken() {
        JwtService service = new JwtService(SECRET, 10);
        String token = service.issueToken("admin") + "x";
        assertTrue(service.validateToken(token).isEmpty());
    }

    @Test
    void rejectsExpiredToken() {
        JwtService service = new JwtService(SECRET, 0);
        String token = service.issueToken("admin");
        assertTrue(service.validateToken(token).isEmpty());
    }

    @Test
    void rejectsGarbageToken() {
        JwtService service = new JwtService(SECRET, 10);
        assertTrue(service.validateToken("not.a.jwt").isEmpty());
    }
}
