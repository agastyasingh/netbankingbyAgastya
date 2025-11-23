package com.main.netbankingapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "https://lightyellow-jaguar-749540.hostingersite.com", allowCredentials = "true")
public class UserApiController {

	@GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("authenticated", false);
            error.put("message", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", oidcUser.getFullName());
        userInfo.put("email", oidcUser.getEmail());
        userInfo.put("picture", oidcUser.getPicture());
        
        userInfo.put("roles", oidcUser.getAuthorities().stream()
            .map(auth -> auth.getAuthority())
            .filter(auth -> auth.startsWith("ROLE_"))
            .map(auth -> auth.substring(5))
            .toList());
        
        return ResponseEntity.ok(userInfo);
    }
    
    @GetMapping("/check-auth")
    public ResponseEntity<Map<String, Boolean>> checkAuth(@AuthenticationPrincipal OidcUser oidcUser) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("authenticated", oidcUser != null);
        return ResponseEntity.ok(response);
    }
}

