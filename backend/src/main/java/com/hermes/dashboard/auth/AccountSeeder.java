package com.hermes.dashboard.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountSeeder implements ApplicationRunner {

    private final UserAccountRepository repository;
    private final PasswordEncoder encoder;
    private final String hostUsername;
    private final String hostPassword;

    public AccountSeeder(UserAccountRepository repository,
                         PasswordEncoder encoder,
                         @Value("${hermes.dashboard.host-username:Akash}") String hostUsername,
                         @Value("${hermes.dashboard.host-password:Free@2006}") String hostPassword) {
        this.repository = repository;
        this.encoder = encoder;
        this.hostUsername = hostUsername;
        this.hostPassword = hostPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!repository.existsByRole(UserAccount.Role.HOST)) {
            repository.save(new UserAccount(hostUsername, encoder.encode(hostPassword),
                    UserAccount.Role.HOST, UserAccount.Status.APPROVED));
        }
    }
}
