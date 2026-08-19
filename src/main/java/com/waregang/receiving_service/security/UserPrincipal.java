package com.waregang.receiving_service.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public record UserPrincipal(
        UUID id,
        String email,
        String warehouseId,
        Collection<SimpleGrantedAuthority> authorities
) {
}