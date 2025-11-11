package com.example.bankcards.repository;

import com.example.bankcards.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    boolean existsByUsernameIgnoreCase(String username);
    Optional<UserAccount> findByUsernameIgnoreCase(String username);
}
