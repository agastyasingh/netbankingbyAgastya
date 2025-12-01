package com.main.netbankingapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
//	@Value("${app.frontend.url}")
//    private String frontendUrl;

    @GetMapping("/login-success")
    public RedirectView loginSuccess(@AuthenticationPrincipal Jwt jwt) {
        // After successful OAuth2 login, redirect to Angular
//        return new RedirectView("http://localhost:4200/dashboard");
        return new RedirectView("https://lightyellow-jaguar-749540.hostingersite.com/dashboard");
    }
    
    @GetMapping("/logout-success")
    public RedirectView logoutSuccess() {
        return new RedirectView("https://lightyellow-jaguar-749540.hostingersite.com/login?logout=true");
    }
}
