package com.hermes.dashboard.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final String SECRET = "a-very-long-secret-for-hs256-signing!!";

    @Test
    void loginDelegatesToAccountsAndIssuesToken() {
        AccountService accounts = mock(AccountService.class);
        JwtService jwt = new JwtService(SECRET, 10);
        when(accounts.login(eq("bob"), eq("pw"), any())).thenAnswer(inv ->
                ((JwtService) inv.getArgument(2)).issueToken("bob"));
        AuthService service = new AuthService(accounts, jwt);

        String token = service.login("bob", "pw");
        assertNotNull(token);
        assertEquals("bob", jwt.validateToken(token).orElseThrow());
    }

    @Test
    void loginReturnsNullWhenAccountsReject() {
        AccountService accounts = mock(AccountService.class);
        when(accounts.login(any(), any(), any())).thenReturn(null);
        assertNull(new AuthService(accounts, new JwtService(SECRET, 10)).login("bob", "bad"));
    }

    @Test
    void isHostChecksAuthenticationName() {
        AccountService accounts = mock(AccountService.class);
        when(accounts.isHost("akash")).thenReturn(true);
        when(accounts.isHost("bob")).thenReturn(false);
        AuthService service = new AuthService(accounts, new JwtService(SECRET, 10));

        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "akash", null, java.util.List.of());
        var bobAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "bob", null, java.util.List.of());
        org.junit.jupiter.api.Assertions.assertTrue(service.isHost(auth));
        org.junit.jupiter.api.Assertions.assertFalse(service.isHost(bobAuth));
        org.junit.jupiter.api.Assertions.assertFalse(service.isHost(null));
    }
}
