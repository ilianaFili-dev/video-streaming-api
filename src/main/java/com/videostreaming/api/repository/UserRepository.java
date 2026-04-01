package com.videostreaming.api.repository;

import com.videostreaming.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCreditCardHash(String creditCardHash);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByCreditCardHash(String creditCardHash);

    @Query("SELECT u FROM User u WHERE u.creditCardHash IS NOT NULL")
    List<User> findAllWithCreditCard();
}