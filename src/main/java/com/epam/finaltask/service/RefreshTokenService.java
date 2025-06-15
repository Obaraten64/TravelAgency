package com.epam.finaltask.service;

import com.epam.finaltask.exception.exceptions.UserSearchException;
import com.epam.finaltask.model.RefreshToken;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.RefreshTokenRepository;
import com.epam.finaltask.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final static RefreshToken EMPTY_TOKEN = RefreshToken.builder().user(new User()).build();
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private final Long expiration;

    public RefreshToken createRefreshToken(String username) {
        User user = findUserByUsername(username);
        RefreshToken refreshToken = RefreshToken.builder()
                .expiryDate(new Date(System.currentTimeMillis() + expiration))
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public void removeRefreshToken(String token) {
        refreshTokenRepository.deleteById(parseUUID(token));
    }

    public boolean isExpired(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findById(parseUUID(token));
        return refreshToken.map(value ->
                        value.getExpiryDate().before(new Date()))
                .orElse(true);

    }

    public boolean isPresent(String username) {
        User user = findUserByUsername(username);
        return refreshTokenRepository.existsByUser(user);
    }

    public String getUsername(String token) {
        return refreshTokenRepository.findById(UUID.fromString(token))
                .orElse(EMPTY_TOKEN).getUser().getUsername();
    }

    private User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserSearchException("User not found!"));
    }

    private UUID parseUUID(String uuid) {
        return Optional.ofNullable(uuid)
                .map(UUID::fromString)
                .orElseThrow(() -> new UserSearchException("Invalid Refresh Token"));
    }
}
