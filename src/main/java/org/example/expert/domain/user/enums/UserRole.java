package org.example.expert.domain.user.enums;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;

import java.util.Arrays;

@RequiredArgsConstructor
public enum UserRole {
    ROLE_ADMIN(Authority.ADMIN),
    ROLE_USER(Authority.USER);

    private final String userRole;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("유효하지 않은 UserRole"));
    }

    public static class Authority {
        public static final String ADMIN = "ROLE_ADMIN";
        public static final String USER = "ROLE_USER";
    }
}
