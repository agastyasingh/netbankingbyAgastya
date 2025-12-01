package com.main.netbankingapp.configuration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.main.netbankingapp.service.JwtService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	
	@Autowired
	private JwtService jwtService;
	
//	@Override
//    public void onAuthenticationSuccess(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        Authentication authentication)
//            throws IOException, ServletException {
//
//        String username = authentication.getName();
//        String jwtToken = jwtService.generateToken(username);
//
//        System.out.println("=== OAuth Success ===");
//        System.out.println("Generated JWT: " + jwtToken);
//
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        String json = "{ \"token\": \"" + jwtToken + "\" }";
//
//        response.getWriter().write(json);
//        response.getWriter().flush();
//    }
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
	                                    HttpServletResponse response,
	                                    Authentication authentication)
	        throws IOException, ServletException {
		
		OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

		 String email = oidcUser.getEmail();
	     String name = oidcUser.getFullName();
	     List<String> roles = oidcUser.getAuthorities()
	                .stream()
	                .map(a -> a.getAuthority())
	                .toList();

	        Map<String, Object> claims = new HashMap<>();
	        claims.put("email", email);
	        claims.put("name", name);
	        claims.put("roles", roles);

	        String token = jwtService.generateToken(claims, email);

	        // redirect to Angular with FULL token
	        response.sendRedirect("https://lightyellow-jaguar-749540.hostingersite.com/dashboard?token=" + token);
	    }

}

