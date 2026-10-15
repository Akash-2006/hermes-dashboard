package com.hermes.dashboard.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final UserAccountRepository repository;
    private final PasswordEncoder encoder;

    public AccountService(UserAccountRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public enum RegisterResult { CREATED, ALREADY_EXISTS }

    /** New accounts always start PENDING until the host approves them. */
    public RegisterResult register(String username, String password) {
        if (repository.findByUsername(username).isPresent()) {
            return RegisterResult.ALREADY_EXISTS;
        }
        repository.save(new UserAccount(username, encoder.encode(password),
                UserAccount.Role.MEMBER, UserAccount.Status.PENDING));
        return RegisterResult.CREATED;
    }

    /** Returns a JWT on success; null when credentials are wrong or the account is not approved. */
    public String login(String username, String password, JwtService jwtService) {
        return repository.findByUsername(username)
                .filter(a -> a.getStatus() == UserAccount.Status.APPROVED)
                .filter(a -> encoder.matches(password, a.getPasswordHash()))
                .map(a -> jwtService.issueToken(a.getUsername()))
                .orElse(null);
    }

    /** Returns false when the account does not exist or is not pending. */
    public boolean decide(String username, boolean approve) {
        return repository.findByUsername(username)
                .filter(a -> a.getStatus() == UserAccount.Status.PENDING)
                .map(a -> {
                    a.setStatus(approve ? UserAccount.Status.APPROVED : UserAccount.Status.REJECTED);
                    repository.save(a);
                    return true;
                })
                .orElse(false);
    }

    public boolean isHost(String username) {
        return repository.findByUsername(username)
                .map(a -> a.getRole() == UserAccount.Role.HOST)
                .orElse(false);
    }

    public java.util.List<UserAccount> pending() {
        return repository.findByStatusOrderByIdAsc(UserAccount.Status.PENDING);
    }
}
