package com.kolyvanova.chatter.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, UUID id);

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);

    Page<User> findAll(Pageable pageable);

    Page<User> findAllByRole(UserRole role, Pageable pageable);

}
