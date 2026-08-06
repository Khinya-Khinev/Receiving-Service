package com.waregang.receiving_service.test_utils.mothers;

import com.waregang.receiving_service.user.domain.Authority;
import com.waregang.receiving_service.security.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

public class UserPrincipalMother {

    public static UserPrincipal worker(String warehouseId) {
        return new UserPrincipal(
                UUID.randomUUID(),
                "test_worker",
                "test_worker@warehouse.com",
                warehouseId,
                List.of(new SimpleGrantedAuthority(Authority.WORKER.name()))
        );
    }

    public static UserPrincipal manager(String warehouseId) {
        return new UserPrincipal(
                UUID.randomUUID(),
                "test_manager",
                "test_manager@warehouse.com",
                warehouseId,
                List.of(new SimpleGrantedAuthority(Authority.MANAGER.name()))
        );
    }
}
