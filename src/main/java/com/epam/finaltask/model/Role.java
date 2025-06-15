package com.epam.finaltask.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.finaltask.model.Permission.*;

@AllArgsConstructor
@Getter
public enum Role {
    USER(Set.of(USER_READ, USER_UPDATE, USER_CREATE, USER_DELETE)),
    ADMIN(Set.of(ADMIN_READ, ADMIN_UPDATE, ADMIN_CREATE, ADMIN_DELETE)),
    MANAGER(Set.of(MANAGER_UPDATE));

    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(permission ->
                        new SimpleGrantedAuthority(permission.toString()))
                .collect(Collectors.toList());
    }
}
