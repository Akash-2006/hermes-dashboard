package com.hermes.dashboard.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    List<UserAccount> findByStatusOrderByIdAsc(UserAccount.Status status);
    boolean existsByRole(UserAccount.Role role);
}
