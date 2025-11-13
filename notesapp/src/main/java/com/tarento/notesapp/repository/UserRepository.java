package com.tarento.notesapp.repository;

import com.tarento.notesapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // Since we use email as the username, we need this method
    // Optional<User> findByEmail(String email);
    boolean existsById(Long id); // already provided by JpaRepository
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username); // optional helper



}