package com.gloperations;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentityController {
    private final CapabilityAuthorizationService authorization;
    IdentityController(CapabilityAuthorizationService authorization) { this.authorization = authorization; }
    @GetMapping("/api/auth/me")
    Map<String,String> me(Authentication a) { return Map.of("username", a.getName()); }
    @GetMapping("/api/identity/users")
    Map<String,String> users(Authentication a) { if (!authorization.allows(a.getName(), "identity.manage", "GLOBAL", null)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN); return Map.of("status","ok"); }
    @GetMapping("/api/identity/roles")
    Map<String,String> roles(Authentication a) { if (!authorization.allows(a.getName(), "identity.manage", "GLOBAL", null)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN); return Map.of("status","ok"); }
}
