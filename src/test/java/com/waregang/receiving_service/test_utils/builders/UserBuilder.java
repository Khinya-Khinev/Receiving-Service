package com.waregang.receiving_service.test_utils.builders;

import com.waregang.receiving_service.user.domain.Authority;
import com.waregang.receiving_service.user.domain.User;
import com.waregang.receiving_service.security.api.dto.RegisterUserRequest;
import java.util.UUID;

public class UserBuilder {
    private String password = "encodedPassword123"; // not encoded yet
    private String nickname = "TestUser";
    private String email = "test@warehouse.com";
    private Authority authority = Authority.WORKER;
    private String warehouseId = "WH-001";

    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    public UserBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public UserBuilder withNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder withAuthority(Authority authority) {
        this.authority = authority;
        return this;
    }

    public UserBuilder withWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
        return this;
    }

    private RegisterUserRequest buildRequest() {
        return new RegisterUserRequest(
                this.email,
                this.password,
                this.nickname,
                this.warehouseId
        );
    }

    public User build() {
        RegisterUserRequest request = buildRequest();

        if (authority == Authority.MANAGER) {
            return User.createManager(request, this.password);
        } else {
            return User.createWorker(request, this.password);
        }
    }

    public User buildBoxCat() {
        this.authority = Authority.WORKER;
        return build();
    }

    public User buildBoxManager() {
        this.authority = Authority.MANAGER;
        return build();
    }

    // Если нужно создать User с конкретным ID (для тестов с предопределенным ID)
    // В реальности ID генерируется внутри, но мы можем использовать рефлексию только для установки ID
    public User buildWithId(UUID id) {
        User user = build();
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
            return user;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set ID on User", e);
        }
    }
}