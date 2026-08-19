package com.waregang.receiving_service.test_utils.mothers;

import com.waregang.receiving_service.security.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class UserPrincipalMother {

    public static UserPrincipal manager(String warehouseId) {
        return new UserPrincipal(
                UUID.randomUUID(),
                "manager@example.com",
                warehouseId,
                List.of(new SimpleGrantedAuthority("MANAGER"))
        );
    }

    public static UserPrincipal worker(String warehouseId) {
        return new UserPrincipal(
                UUID.randomUUID(),
                "worker@example.com",
                warehouseId,
                List.of(new SimpleGrantedAuthority("WORKER"))
        );
    }
}
