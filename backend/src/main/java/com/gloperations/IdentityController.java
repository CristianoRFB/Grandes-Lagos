package com.gloperations;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentityController {
    @GetMapping("/api/auth/me")
    Map<String,String> me(Authentication a) { return Map.of("username", a.getName()); }
    @GetMapping("/api/identity/users")
    Map<String,String> users(Authentication a) { if (a.getAuthorities().stream().noneMatch(x -> x.getAuthority().equals("identity.manage"))) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN); return Map.of("status","ok"); }
    @GetMapping("/api/identity/roles")
    Map<String,String> roles(Authentication a) { return Map.of("status","ok"); }
}
