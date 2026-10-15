package com.hermes.dashboard.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {

    private final UserAccountRepository repository = mock(UserAccountRepository.class);
    private final AccountService service = new AccountService(repository, new BCryptPasswordEncoder());
    private final JwtService jwt = new JwtService("a-very-long-secret-for-hs256-signing!!", 10);

    @Test
    void registerCreatesPendingAccount() {
        when(repository.findByUsername("bob")).thenReturn(Optional.empty());
        assertEquals(AccountService.RegisterResult.CREATED, service.register("bob", "pw"));
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(a ->
                a.getStatus() == UserAccount.Status.PENDING
                        && a.getRole() == UserAccount.Role.MEMBER
                        && !a.getPasswordHash().equals("pw")));
    }

    @Test
    void duplicateUsernameRejected() {
        when(repository.findByUsername("bob"))
                .thenReturn(Optional.of(new UserAccount("bob", "h", UserAccount.Role.MEMBER, UserAccount.Status.APPROVED)));
        assertEquals(AccountService.RegisterResult.ALREADY_EXISTS, service.register("bob", "pw"));
        verify(repository, never()).save(any());
    }

    @Test
    void pendingAccountCannotLogIn() {
        when(repository.findByUsername("bob")).thenReturn(Optional.of(
                new UserAccount("bob", new BCryptPasswordEncoder().encode("pw"),
                        UserAccount.Role.MEMBER, UserAccount.Status.PENDING)));
        assertNull(service.login("bob", "pw", jwt));
    }

    @Test
    void approvedAccountCanLogIn() {
        when(repository.findByUsername("bob")).thenReturn(Optional.of(
                new UserAccount("bob", new BCryptPasswordEncoder().encode("pw"),
                        UserAccount.Role.MEMBER, UserAccount.Status.APPROVED)));
        String token = service.login("bob", "pw", jwt);
        assertNotNull(token);
        assertEquals("bob", jwt.validateToken(token).orElseThrow());
    }

    @Test
    void wrongPasswordRejected() {
        when(repository.findByUsername("bob")).thenReturn(Optional.of(
                new UserAccount("bob", new BCryptPasswordEncoder().encode("pw"),
                        UserAccount.Role.MEMBER, UserAccount.Status.APPROVED)));
        assertNull(service.login("bob", "nope", jwt));
    }

    @Test
    void hostApprovesPendingAccount() {
        UserAccount pending = new UserAccount("bob", "h", UserAccount.Role.MEMBER, UserAccount.Status.PENDING);
        when(repository.findByUsername("bob")).thenReturn(Optional.of(pending));
        assertTrue(service.decide("bob", true));
        assertEquals(UserAccount.Status.APPROVED, pending.getStatus());
    }

    @Test
    void hostRejectsPendingAccount() {
        UserAccount pending = new UserAccount("bob", "h", UserAccount.Role.MEMBER, UserAccount.Status.PENDING);
        when(repository.findByUsername("bob")).thenReturn(Optional.of(pending));
        assertTrue(service.decide("bob", false));
        assertEquals(UserAccount.Status.REJECTED, pending.getStatus());
    }

    @Test
    void decideFailsForUnknownOrNonPending() {
        when(repository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(repository.findByUsername("approved")).thenReturn(Optional.of(
                new UserAccount("approved", "h", UserAccount.Role.MEMBER, UserAccount.Status.APPROVED)));
        assertFalse(service.decide("ghost", true));
        assertFalse(service.decide("approved", true));
    }

    @Test
    void hostRoleDetection() {
        when(repository.findByUsername("akash")).thenReturn(Optional.of(
                new UserAccount("akash", "h", UserAccount.Role.HOST, UserAccount.Status.APPROVED)));
        when(repository.findByUsername("bob")).thenReturn(Optional.of(
                new UserAccount("bob", "h", UserAccount.Role.MEMBER, UserAccount.Status.APPROVED)));
        assertTrue(service.isHost("akash"));
        assertFalse(service.isHost("bob"));
    }

    @Test
    void pendingListOnlyPending() {
        when(repository.findByStatusOrderByIdAsc(UserAccount.Status.PENDING))
                .thenReturn(List.of(new UserAccount("bob", "h", UserAccount.Role.MEMBER, UserAccount.Status.PENDING)));
        assertEquals(1, service.pending().size());
    }
}
