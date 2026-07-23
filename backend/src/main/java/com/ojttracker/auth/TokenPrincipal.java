package com.ojttracker.auth;

import java.time.Instant;

public record TokenPrincipal(Role role, Long siteId, Instant exp) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
