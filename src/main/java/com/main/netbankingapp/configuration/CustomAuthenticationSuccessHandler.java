package com.main.netbankingapp.configuration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

//	@Value("${app.frontend.url}")
//	private String frontendUrl;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

//        // Get session ID
//        String sessionId = request.getSession().getId();
//        
//        // Set CORS headers
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
//        response.setHeader("Access-Control-Allow-Credentials", "true");
//        
//        // Redirect to Angular with session info
//        response.sendRedirect("http://localhost:4200/dashboard?sessionId=" + sessionId);

		String sessionId = request.getSession().getId();

		// Updated CORS header
		response.setHeader("Access-Control-Allow-Origin", "https://lightyellow-jaguar-749540.hostingersite.com");
		response.setHeader("Access-Control-Allow-Credentials", "true");

		// Redirect to Hostinger frontend
		response.sendRedirect("https://lightyellow-jaguar-749540.hostingersite.com/dashboard?sessionId=" + sessionId);
	}
}
