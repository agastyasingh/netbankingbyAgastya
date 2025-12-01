package com.main.netbankingapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "https://lightyellow-jaguar-749540.hostingersite.com", allowCredentials = "true")
public class UserApiController {

	@GetMapping("/info")
	public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal Jwt jwt) {

	    if (jwt == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
	            "authenticated", false
	        ));
	    }

	    Map<String, Object> user = new HashMap<>();
	    user.put("email", jwt.getClaim("email"));
	    user.put("name", jwt.getClaim("name"));
	    user.put("roles", jwt.getClaimAsStringList("roles"));

	    return ResponseEntity.ok(user);
	}

    
	@GetMapping("/check-auth")
	public ResponseEntity<Map<String, Boolean>> checkAuth(@AuthenticationPrincipal Jwt jwt) {
	    Map<String, Boolean> response = new HashMap<>();
	    response.put("authenticated", jwt != null);
	    return ResponseEntity.ok(response);
	}

}

