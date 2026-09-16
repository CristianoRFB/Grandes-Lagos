package com.gloperations;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.userdetails.User;

/** Session identity is immutable; capabilities are never stored here. Spring erases credentials. */
public final class IdentityPrincipal extends User {
    private static final long serialVersionUID = 1L;
    private final UUID id;

    public IdentityPrincipal(UUID id, String username, String encodedPassword, boolean enabled) {
        super(username, encodedPassword, enabled, true, true, true, List.of());
        this.id = id;
    }

    public UUID id() { return id; }
}
