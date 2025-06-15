package com.epam.finaltask.repository;

import com.epam.finaltask.model.RefreshToken;

import com.epam.finaltask.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    boolean existsByUser(User user);
}
