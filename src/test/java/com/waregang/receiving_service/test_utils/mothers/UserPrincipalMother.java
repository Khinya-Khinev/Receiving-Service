package com.waregang.receiving_service.test_utils.mothers;

import com.waregang.receiving_service.security.Authority;
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
                List.of(new SimpleGrantedAuthority(Authority.BOX_CAT.name()))
        );
    }

    public static UserPrincipal manager(String warehouseId) {
        return new UserPrincipal(
                UUID.randomUUID(),
                "test_manager",
                "test_manager@warehouse.com",
                warehouseId,
                List.of(new SimpleGrantedAuthority(Authority.BOX_MANAGER.name()))
        );
    }
}
